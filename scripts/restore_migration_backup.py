#!/usr/bin/env python3

import argparse
import os
import shutil
import tempfile
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path


BACKUP_DIRECTORY = "project_mystery_backups"


@dataclass(frozen=True)
class RestoreResult:
    snapshot: Path
    safety_backup: Path | None
    restored_files: int
    removed_files: int
    dry_run: bool


def _inside(root: Path, path: Path) -> bool:
    try:
        path.relative_to(root)
        return True
    except ValueError:
        return False


def _assert_safe_path(root: Path, path: Path) -> None:
    if not _inside(root, path):
        raise ValueError(f"path escapes root: {path}")
    relative = path.relative_to(root)
    current = root
    for part in relative.parts:
        current = current / part
        if current.is_symlink():
            raise ValueError(f"symbolic links are not allowed: {current}")


def _allowed_relative(relative: Path) -> bool:
    parts = relative.parts
    if parts in {("level.dat",), ("level.dat_old",)}:
        return True
    if len(parts) >= 2 and parts[0] == "playerdata":
        return relative.name.endswith((".dat", ".dat_old"))
    if len(parts) >= 2 and parts[0] == "data":
        name = relative.name
        return (name.startswith("lord_of_mysteries")
                or name.startswith("project_mystery")) and name.endswith(
                    (".dat", ".dat_old"))
    return False


def _collect_files(root: Path) -> dict[Path, Path]:
    files = {}
    for path in root.rglob("*"):
        if path.name == "manifest.properties" or not path.is_file():
            continue
        _assert_safe_path(root, path)
        relative = path.relative_to(root)
        if not _allowed_relative(relative):
            raise ValueError(f"snapshot contains unsupported file: {relative}")
        files[relative] = path
    return files


def _collect_world_files(world_root: Path) -> dict[Path, Path]:
    files = {}
    candidates = [world_root / "level.dat", world_root / "level.dat_old"]
    for directory in (world_root / "playerdata", world_root / "data"):
        if directory.is_dir() and not directory.is_symlink():
            candidates.extend(path for path in directory.rglob("*")
                              if path.is_file())
    for path in candidates:
        if not path.is_file():
            continue
        _assert_safe_path(world_root, path)
        relative = path.relative_to(world_root)
        if _allowed_relative(relative):
            files[relative] = path
    return files


def _read_manifest(snapshot: Path, file_count: int) -> dict[str, str]:
    manifest_path = snapshot / "manifest.properties"
    if not manifest_path.is_file() or manifest_path.is_symlink():
        raise ValueError(f"missing safe manifest: {manifest_path}")
    values = {}
    for raw_line in manifest_path.read_text(encoding="utf-8").splitlines():
        if not raw_line.strip() or "=" not in raw_line:
            continue
        key, value = raw_line.split("=", 1)
        values[key.strip()] = value.strip()
    if int(values.get("schema", "0")) < 1:
        raise ValueError("manifest schema must be positive")
    if int(values.get("files", "-1")) != file_count:
        raise ValueError("manifest file count does not match snapshot")
    return values


def _resolve_snapshot(world_root: Path, snapshot_name: str) -> Path:
    if not snapshot_name or Path(snapshot_name).name != snapshot_name:
        raise ValueError("snapshot must be a direct backup directory name")
    backup_root = world_root / BACKUP_DIRECTORY
    if backup_root.is_symlink() or not backup_root.is_dir():
        raise ValueError(f"backup root is unavailable: {backup_root}")
    snapshot = backup_root / snapshot_name
    _assert_safe_path(backup_root, snapshot)
    if snapshot.is_symlink() or not snapshot.is_dir():
        raise ValueError(f"snapshot is unavailable: {snapshot}")
    return snapshot


def _copy_tree(files: dict[Path, Path], destination: Path) -> None:
    for relative, source in files.items():
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)


def _safety_backup(world_root: Path, files: dict[Path, Path], now: datetime) -> Path:
    backup_root = world_root / BACKUP_DIRECTORY
    base = "pre-restore-" + now.astimezone(timezone.utc).strftime(
        "%Y%m%dT%H%M%SZ")
    destination = backup_root / base
    suffix = 1
    while destination.exists():
        destination = backup_root / f"{base}-{suffix}"
        suffix += 1
    destination.mkdir(parents=True)
    _copy_tree(files, destination)
    (destination / "manifest.properties").write_text(
        "reason=pre_restore_safety_backup\n"
        f"created_utc={now.astimezone(timezone.utc).isoformat()}\n"
        f"files={len(files)}\n",
        encoding="utf-8",
        newline="\n",
    )
    return destination


def restore_snapshot(world_root: Path, snapshot_name: str,
                     dry_run: bool = False,
                     now: datetime | None = None) -> RestoreResult:
    root = world_root.expanduser().resolve()
    if root.is_symlink() or not root.is_dir():
        raise ValueError(f"world root is unavailable: {root}")
    snapshot = _resolve_snapshot(root, snapshot_name)
    snapshot_files = _collect_files(snapshot)
    _read_manifest(snapshot, len(snapshot_files))
    current_files = _collect_world_files(root)
    removed = sorted(set(current_files) - set(snapshot_files))
    if dry_run:
        return RestoreResult(snapshot, None, len(snapshot_files), len(removed), True)

    timestamp = now or datetime.now(timezone.utc)
    safety = _safety_backup(root, current_files, timestamp)
    staging = Path(tempfile.mkdtemp(prefix=".pm-restore-", dir=root))
    try:
        _copy_tree(snapshot_files, staging)
        for relative in removed:
            current_files[relative].unlink()
        for relative in sorted(snapshot_files):
            source = staging / relative
            target = root / relative
            _assert_safe_path(root, target)
            target.parent.mkdir(parents=True, exist_ok=True)
            os.replace(source, target)
    finally:
        shutil.rmtree(staging, ignore_errors=True)
    return RestoreResult(snapshot, safety, len(snapshot_files), len(removed), False)


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Safely restore a Project Mystery schema snapshot")
    parser.add_argument("world_root", type=Path)
    parser.add_argument("snapshot",
                        help="direct child directory under project_mystery_backups")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--confirm", action="store_true",
                        help="required for an actual restore")
    args = parser.parse_args()
    if not args.dry_run and not args.confirm:
        parser.error("actual restore requires --confirm")
    result = restore_snapshot(args.world_root, args.snapshot, args.dry_run)
    action = "would restore" if result.dry_run else "restored"
    print(f"{action} {result.restored_files} files from {result.snapshot}")
    print(f"files removed because absent from snapshot: {result.removed_files}")
    if result.safety_backup is not None:
        print(f"pre-restore safety backup: {result.safety_backup}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
