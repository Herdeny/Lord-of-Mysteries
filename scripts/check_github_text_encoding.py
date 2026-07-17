#!/usr/bin/env python3

import argparse
import json
import os
import re
import subprocess


CORRUPTION_PATTERNS = (
    (re.compile("\ufffd"), "Unicode replacement character"),
    (re.compile(r"\?{2,}"), "consecutive question marks"),
    (re.compile(r"(?:\?\s*){3,}"), "question-mark corruption sequence"),
)


def parse_args():
    parser = argparse.ArgumentParser(
        description="Reject likely encoding corruption in GitHub Issues and comments."
    )
    parser.add_argument(
        "--repository",
        default=os.environ.get("GITHUB_REPOSITORY"),
        help="GitHub repository in owner/name form (defaults to GITHUB_REPOSITORY).",
    )
    args = parser.parse_args()
    if not args.repository or "/" not in args.repository:
        parser.error("--repository must use owner/name form")
    return args


def fetch_pages(endpoint):
    result = subprocess.run(
        ["gh", "api", "--paginate", "--slurp", endpoint],
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    pages = json.loads(result.stdout)
    return [entry for page in pages for entry in page]


def corruption_reason(text):
    for pattern, reason in CORRUPTION_PATTERNS:
        if pattern.search(text or ""):
            return reason
    return None


def main():
    args = parse_args()
    issues = fetch_pages(
        f"repos/{args.repository}/issues?state=all&per_page=100"
    )
    comments = fetch_pages(
        f"repos/{args.repository}/issues/comments?per_page=100"
    )

    failures = []
    for issue in issues:
        if "pull_request" in issue:
            continue
        for field in ("title", "body"):
            reason = corruption_reason(issue.get(field))
            if reason:
                failures.append(
                    f"Issue #{issue['number']} {field}: {reason} ({issue['html_url']})"
                )

    for comment in comments:
        reason = corruption_reason(comment.get("body"))
        if reason:
            failures.append(
                f"Issue comment {comment['id']}: {reason} ({comment['html_url']})"
            )

    if failures:
        print("GitHub text encoding audit failed:")
        for failure in failures:
            print(f"- {failure}")
        raise SystemExit(1)

    print(
        "GitHub text encoding audit passed: "
        f"{len([issue for issue in issues if 'pull_request' not in issue])} issues, "
        f"{len(comments)} comments"
    )


if __name__ == "__main__":
    main()
