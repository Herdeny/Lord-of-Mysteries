#!/usr/bin/env python3

import argparse
import re
import subprocess
import sys
from pathlib import Path

from run_server_smoke import EXPECTED_PLAYER_SCHEMA, ROOT, RUN_DIR


SEED_PATTERN = re.compile(r"Seed: \[(-?\d+)\]")


def run_once(timeout: int, iteration: int) -> tuple[str, str]:
    command = [sys.executable, str(ROOT / "scripts/run_server_smoke.py"),
               "--timeout", str(timeout)]
    process = subprocess.run(
        command, cwd=ROOT, text=True, encoding="utf-8", errors="replace",
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
    print(f"--- dedicated restart pass {iteration} ---")
    print(process.stdout, end="")
    if process.returncode != 0:
        raise RuntimeError(
            f"dedicated restart pass {iteration} failed with {process.returncode}")
    matches = SEED_PATTERN.findall(process.stdout)
    if not matches:
        raise RuntimeError(f"dedicated restart pass {iteration} has no seed evidence")
    marker = (RUN_DIR / "world/project_mystery_backups"
              / f"schema-{EXPECTED_PLAYER_SCHEMA}.complete")
    if not marker.is_file():
        raise RuntimeError(f"dedicated restart pass {iteration} has no schema marker")
    return matches[-1], marker.read_text(encoding="utf-8").strip()


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(errors="replace")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(errors="replace")
    parser = argparse.ArgumentParser()
    parser.add_argument("--timeout", type=int, default=180)
    args = parser.parse_args()
    if args.timeout < 30:
        parser.error("--timeout must be at least 30 seconds")
    first_seed, first_snapshot = run_once(args.timeout, 1)
    second_seed, second_snapshot = run_once(args.timeout, 2)
    if first_seed != second_seed:
        raise RuntimeError(
            f"world seed changed across restart: {first_seed} != {second_seed}")
    if first_snapshot != second_snapshot:
        raise RuntimeError("schema snapshot marker changed across restart")
    print("dedicated restart matrix passed: stable world seed, idempotent schema "
          f"snapshot {first_snapshot}, two clean saves and two clean shutdowns")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
