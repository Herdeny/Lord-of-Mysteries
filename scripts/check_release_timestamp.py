#!/usr/bin/env python3

import argparse
import json
import subprocess
import sys
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
STATUS_PATH = ROOT / "project-status.json"


def git(*arguments):
    return subprocess.check_output(
        ["git", *arguments],
        cwd=ROOT,
        text=True,
        encoding="utf-8",
    ).strip()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--require-head",
        action="store_true",
        help="要求 project-status.json 的最后修改提交就是当前 HEAD",
    )
    parser.add_argument("--max-drift-seconds", type=int, default=300)
    args = parser.parse_args()

    status = json.loads(STATUS_PATH.read_text(encoding="utf-8"))
    local_time = datetime.fromisoformat(status["last_updated"])
    utc_time = datetime.fromisoformat(status["last_updated_utc"].replace("Z", "+00:00"))
    if local_time.astimezone(utc_time.tzinfo) != utc_time:
        print("last_updated 与 last_updated_utc 不一致", file=sys.stderr)
        return 1

    status_commit = git("log", "-1", "--format=%H", "--", "project-status.json")
    commit_time = datetime.fromisoformat(git("show", "-s", "--format=%cI", status_commit))
    drift = abs((utc_time - commit_time.astimezone(utc_time.tzinfo)).total_seconds())
    if drift > args.max_drift_seconds:
        print(
            f"项目更新时间与状态提交相差 {drift:.0f} 秒，"
            f"超过 {args.max_drift_seconds} 秒；发布前请运行 scripts/stamp_release.py",
            file=sys.stderr,
        )
        return 1

    head = git("rev-parse", "HEAD")
    if args.require_head and status_commit != head:
        print(
            "project-status.json 未在最终提交中更新；"
            "发布前请运行 scripts/stamp_release.py 并将同步文件纳入同一提交",
            file=sys.stderr,
        )
        return 1

    print(f"release timestamp checked: drift={drift:.0f}s commit={status_commit[:7]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
