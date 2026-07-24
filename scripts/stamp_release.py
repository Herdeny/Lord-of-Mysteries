#!/usr/bin/env python3

import json
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from zoneinfo import ZoneInfo


ROOT = Path(__file__).resolve().parents[1]
STATUS_PATH = ROOT / "project-status.json"


def main():
    status = json.loads(STATUS_PATH.read_text(encoding="utf-8"))
    local_now = datetime.now(ZoneInfo("Europe/London")).replace(microsecond=0)
    utc_now = local_now.astimezone(timezone.utc)
    status["last_updated"] = local_now.isoformat()
    status["last_updated_utc"] = utc_now.isoformat().replace("+00:00", "Z")
    STATUS_PATH.write_text(
        json.dumps(status, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
        newline="\n",
    )
    result = subprocess.run(
        [sys.executable, str(ROOT / "scripts" / "sync_project_metadata.py")],
        cwd=ROOT,
        check=False,
    )
    if result.returncode:
        return result.returncode
    print(f"release timestamp stamped: {status['last_updated']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
