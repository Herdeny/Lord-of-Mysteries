# Repository Instructions

- `project-status.json` is the canonical source for project version, stage, technical baseline, and update timestamps.
- Any user-visible feature change must update the relevant content in `README.md`, `docs/`, `wiki/`, and `CHANGELOG.md`.
- After changing `project-status.json`, run `python scripts/sync_project_metadata.py`.
- Before publishing, run `python scripts/sync_project_metadata.py --check` and `./gradlew clean build`.
- Keep GitHub Pages and GitHub Wiki content aligned with the implemented code; existing repository behavior wins over design documents.
