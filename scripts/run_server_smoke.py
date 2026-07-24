#!/usr/bin/env python3

import argparse
import os
import queue
import re
import signal
import subprocess
import sys
import threading
import time
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RUN_DIR = ROOT / "run"
NETWORK_PROTOCOL_SOURCE = (
    ROOT
    / "src/main/java/top/aurora/lordofmysteries/network/NetworkProtocol.java"
)
PLAYER_DATA_SOURCE = (
    ROOT
    / "src/main/java/top/aurora/lordofmysteries/player/PlayerMysteryData.java"
)
DATA_ROOT = ROOT / "src/main/resources/data/lord_of_mysteries"


def expected_network_metadata():
    source = NETWORK_PROTOCOL_SOURCE.read_text(encoding="utf-8")
    version_match = re.search(r'VERSION\s*=\s*"(\d+)"', source)
    packet_count_match = re.search(r'PACKET_COUNT\s*=\s*(\d+)', source)
    if version_match is None or packet_count_match is None:
        raise RuntimeError("NetworkProtocol version metadata was not found")
    return version_match.group(1), packet_count_match.group(1)


def expected_player_schema():
    source = PLAYER_DATA_SOURCE.read_text(encoding="utf-8")
    schema_match = re.search(r"CURRENT_SCHEMA_VERSION\s*=\s*(\d+)", source)
    if schema_match is None:
        raise RuntimeError("PlayerMysteryData schema metadata was not found")
    return schema_match.group(1)


EXPECTED_PROTOCOL, EXPECTED_PACKET_COUNT = expected_network_metadata()
EXPECTED_PLAYER_SCHEMA = expected_player_schema()
EXPECTED_COMMISSION_COUNT = len(list((DATA_ROOT / "commissions").glob("*.json")))
EXPECTED_QUEST_COUNT = len(list((DATA_ROOT / "quests").glob("*.json")))
EXPECTED = {
    "commissions": f"Loaded {EXPECTED_COMMISSION_COUNT} commission definitions",
    "quests": f"Loaded {EXPECTED_QUEST_COUNT} quest chain definitions",
    "ready": "Done (",
    "servercheck": (
        "PROJECT_MYSTERY_SERVERCHECK_OK "
        f"commissions={EXPECTED_COMMISSION_COUNT} quests={EXPECTED_QUEST_COUNT} "
        f"protocol={EXPECTED_PROTOCOL} packets={EXPECTED_PACKET_COUNT} "
        "overworld=true"
    ),
    "party_storage": "party_storage=true active_parties=",
    "party_members": "party_members=",
    "world_event": "world_event=",
    "city_service": "city_service_version=",
    "command_loop": "There are 0 of a max of",
    "seed": "Seed: [",
    "save": "Saved the game",
}
FATAL_MARKERS = (
    "[Server thread/ERROR]",
    "Encountered an unexpected exception",
    "Failed to start the minecraft server",
)


def stop_process_tree(process):
    if process.poll() is not None:
        return
    if os.name == "nt":
        subprocess.run(
            ["taskkill", "/PID", str(process.pid), "/T", "/F"],
            check=False,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    else:
        os.killpg(process.pid, signal.SIGTERM)
    try:
        process.wait(timeout=10)
    except subprocess.TimeoutExpired:
        if os.name != "nt":
            os.killpg(process.pid, signal.SIGKILL)
        process.wait(timeout=10)


def stream_output(process, output_queue):
    assert process.stdout is not None
    for line in process.stdout:
        output_queue.put(line)
    output_queue.put(None)


def verify_migration_backup():
    backup_root = RUN_DIR / "world" / "project_mystery_backups"
    marker = backup_root / f"schema-{EXPECTED_PLAYER_SCHEMA}.complete"
    if not marker.is_file():
        return False, f"missing migration marker: {marker}"
    snapshot_name = marker.read_text(encoding="utf-8").strip()
    if not snapshot_name:
        return False, f"empty migration marker: {marker}"
    snapshot = (backup_root / snapshot_name).resolve()
    if snapshot.parent != backup_root.resolve() or not snapshot.is_dir():
        return False, f"invalid migration snapshot path: {snapshot}"
    manifest = snapshot / "manifest.properties"
    if not manifest.is_file():
        return False, f"missing migration manifest: {manifest}"
    manifest_text = manifest.read_text(encoding="utf-8")
    if f"schema={EXPECTED_PLAYER_SCHEMA}\n" not in manifest_text:
        return False, f"migration manifest schema mismatch: {manifest}"
    return True, snapshot


def run(timeout_seconds):
    RUN_DIR.mkdir(exist_ok=True)
    (RUN_DIR / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    wrapper = "gradlew.bat" if os.name == "nt" else "./gradlew"
    command = [wrapper, "runServer", "--no-daemon", "--console=plain"]
    creation_flags = subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0
    process = subprocess.Popen(
        command,
        cwd=ROOT,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
        bufsize=1,
        creationflags=creation_flags,
        start_new_session=os.name != "nt",
    )
    output_queue = queue.Queue()
    threading.Thread(
        target=stream_output, args=(process, output_queue), daemon=True
    ).start()
    observed = set()
    fatal_lines = []
    deadline = time.monotonic() + timeout_seconds
    checks_sent = False
    stop_sent = False
    try:
        while time.monotonic() < deadline:
            try:
                line = output_queue.get(timeout=0.25)
            except queue.Empty:
                line = ""
            if line is None:
                break
            if line:
                print(line, end="", flush=True)
                for name, marker in EXPECTED.items():
                    if marker in line:
                        observed.add(name)
                if any(marker in line for marker in FATAL_MARKERS):
                    fatal_lines.append(line.strip())
            if "ready" in observed and not checks_sent:
                assert process.stdin is not None
                process.stdin.write("pm servercheck\n")
                process.stdin.write("list\n")
                process.stdin.write("seed\n")
                process.stdin.write("save-all flush\n")
                process.stdin.flush()
                checks_sent = True
            if observed == set(EXPECTED) and not stop_sent and not fatal_lines:
                assert process.stdin is not None
                process.stdin.write("stop\n")
                process.stdin.flush()
                stop_sent = True
            if process.poll() is not None:
                break
        if fatal_lines:
            print("server smoke detected fatal server-thread output:", file=sys.stderr)
            for line in fatal_lines:
                print(f"  {line}", file=sys.stderr)
            return 1
        if not stop_sent:
            missing = sorted(set(EXPECTED) - observed)
            print(f"server smoke failed; missing markers: {missing}", file=sys.stderr)
            return 1
        try:
            return_code = process.wait(timeout=30)
        except subprocess.TimeoutExpired:
            print("server accepted readiness but did not stop cleanly", file=sys.stderr)
            return 1
        if return_code != 0:
            print(f"server smoke process exited with {return_code}", file=sys.stderr)
            return 1
        backup_valid, backup_evidence = verify_migration_backup()
        if not backup_valid:
            print(f"server smoke failed; {backup_evidence}", file=sys.stderr)
            return 1
        print(
            "server smoke passed: definitions loaded, runtime diagnostics passed, "
            "migration backup verified, command loop responded, world flushed, "
            f"seed captured, and server stopped cleanly ({backup_evidence})"
        )
        return 0
    finally:
        stop_process_tree(process)


def main():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(errors="replace")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(errors="replace")
    parser = argparse.ArgumentParser()
    parser.add_argument("--timeout", type=int, default=180)
    args = parser.parse_args()
    if args.timeout < 30:
        parser.error("--timeout must be at least 30 seconds")
    return run(args.timeout)


if __name__ == "__main__":
    raise SystemExit(main())
