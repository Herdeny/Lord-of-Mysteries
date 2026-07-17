#!/usr/bin/env python3

import tempfile
from datetime import datetime, timezone
from pathlib import Path

from restore_migration_backup import restore_snapshot


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def main() -> int:
    with tempfile.TemporaryDirectory(prefix="pm-rollback-") as temporary:
        world = Path(temporary) / "world"
        snapshot = world / "project_mystery_backups" / "schema-17-fixture"
        write(snapshot / "level.dat", "before-level")
        write(snapshot / "playerdata/test.dat", "before-player")
        write(snapshot / "data/lord_of_mysteries_parties.dat", "before-party")
        write(snapshot / "manifest.properties",
              "schema=17\ncreated_utc=2026-07-17T00:00:00Z\nfiles=3\nbytes=36\n")
        write(world / "level.dat", "after-level")
        write(world / "playerdata/test.dat", "after-player")
        write(world / "playerdata/new-player.dat", "after-new-player")
        write(world / "data/lord_of_mysteries_parties.dat", "after-party")

        preview = restore_snapshot(world, snapshot.name, dry_run=True)
        if preview.restored_files != 3 or preview.removed_files != 1:
            raise RuntimeError("rollback dry-run inventory is incorrect")
        result = restore_snapshot(
            world, snapshot.name, now=datetime(
                2026, 7, 17, tzinfo=timezone.utc))
        expected = {
            "level.dat": "before-level",
            "playerdata/test.dat": "before-player",
            "data/lord_of_mysteries_parties.dat": "before-party",
        }
        for relative, content in expected.items():
            if (world / relative).read_text(encoding="utf-8") != content:
                raise RuntimeError(f"rollback did not restore {relative}")
        if (world / "playerdata/new-player.dat").exists():
            raise RuntimeError("rollback did not remove post-snapshot player data")
        if result.safety_backup is None:
            raise RuntimeError("rollback did not create a safety backup")
        if (result.safety_backup / "level.dat").read_text(
                encoding="utf-8") != "after-level":
            raise RuntimeError("safety backup did not preserve current data")
        try:
            restore_snapshot(world, "../schema-17-fixture", dry_run=True)
        except ValueError:
            pass
        else:
            raise RuntimeError("rollback accepted a traversal snapshot")
    print("save rollback checked: dry-run, exact restore, safety backup, "
          "extra-file removal, and traversal rejection passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
