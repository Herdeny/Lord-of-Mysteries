#!/usr/bin/env python3

import argparse
import os
import queue
import signal
import subprocess
import sys
import threading
import time
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RUN_DIR = ROOT / "run"
EXPECTED = {
    "commissions": "Loaded 3 commission definitions",
    "quests": "Loaded 3 quest chain definitions",
    "ready": "Done (",
    "servercheck": (
        "PROJECT_MYSTERY_SERVERCHECK_OK commissions=3 quests=3 "
        "protocol=6 packets=11 overworld=true"
    ),
    "party_storage": "party_storage=true active_parties=",
    "party_members": "party_members=",
    "command_loop": "There are 0 of a max of",
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
        print(
            "server smoke passed: definitions loaded, runtime diagnostics passed, "
            "command loop responded, world flushed, and server stopped cleanly"
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
