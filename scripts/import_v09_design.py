#!/usr/bin/env python3

import argparse
import hashlib
import json
import shutil
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
FULL_NAME = "Project_Mystery_Design_Doc_v0_9.md"
INCREMENTAL_NAME = "Project_Mystery_v0_9_Incremental_Additions.md"
MANIFEST_NAME = "Project_Mystery_v0_9_manifest.json"
BEGIN_MARKER = b"<!-- BEGIN v0.8 BASELINE: SHA-256 recorded in chapter 81 -->\n"
END_MARKER = b"<!-- END v0.8 BASELINE -->"


def digest(payload):
    return hashlib.sha256(payload).hexdigest()


def line_count(payload):
    return payload.count(b"\n") + 1


def validate(source_dir):
    manifest_path = source_dir / MANIFEST_NAME
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    full_path = source_dir / FULL_NAME
    incremental_path = source_dir / INCREMENTAL_NAME
    full = full_path.read_bytes()
    incremental = incremental_path.read_bytes()

    if manifest.get("version") != "0.9":
        raise ValueError("manifest version must be 0.9")
    if len(full) != manifest.get("full_bytes"):
        raise ValueError("full document byte count does not match manifest")
    if line_count(full) != manifest.get("full_lines"):
        raise ValueError("full document line count does not match manifest")
    if digest(full) != manifest.get("full_sha256"):
        raise ValueError("full document SHA-256 does not match manifest")

    start = full.index(BEGIN_MARKER) + len(BEGIN_MARKER)
    end = full.index(END_MARKER, start)
    baseline = full[start:end]
    if len(baseline) != manifest.get("baseline_bytes"):
        raise ValueError("embedded v0.8 byte count does not match manifest")
    if line_count(baseline) != manifest.get("baseline_lines"):
        raise ValueError("embedded v0.8 line count does not match manifest")
    if digest(baseline) != manifest.get("baseline_sha256"):
        raise ValueError("embedded v0.8 SHA-256 does not match manifest")
    if not manifest.get("baseline_embedded_exactly"):
        raise ValueError("manifest does not assert exact v0.8 embedding")

    full_incremental = full[full.index(b"# J. "):]
    standalone_incremental = incremental[incremental.index(b"# J. "):]
    if full_incremental != standalone_incremental:
        raise ValueError("standalone additions differ from the v0.9 suffix")
    return manifest


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--import-from", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    source_dir = args.import_from or DOCS
    try:
        manifest = validate(source_dir)
        if args.import_from:
            DOCS.mkdir(parents=True, exist_ok=True)
            for name in (FULL_NAME, INCREMENTAL_NAME, MANIFEST_NAME):
                shutil.copyfile(source_dir / name, DOCS / name)
            validate(DOCS)
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as error:
        print(f"v0.9 design validation failed: {error}", file=sys.stderr)
        return 1

    action = "imported" if args.import_from else "checked"
    print(
        f"v0.9 design {action}: {manifest['full_bytes']} bytes, "
        f"{manifest['full_lines']} lines, chapters {manifest['new_chapters']}, "
        "embedded v0.8 baseline verified"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
