"""Replay an upstream diff (base ref -> target ref) onto a port directory with per-file 3-way merges.

Usage: python tools/replay_upstream.py <port dir> <base ref> <target ref>

For each file upstream changed: base = file at <base ref>, theirs = file at <target ref>, ours = the port's file.
Files missing from the port (excluded or deleted by the port) are reported and skipped. Conflicts are left with
markers and listed at the end.
"""
import os
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UPSTREAM = os.path.join(ROOT, "upstream")


def git(*args, binary=False):
    out = subprocess.run(["git", "-C", UPSTREAM, *args], capture_output=True, check=True).stdout
    return out if binary else out.decode("utf-8")


def show(ref, path):
    return git("show", f"{ref}:{path}", binary=True)


def main():
    port, base, target = sys.argv[1:4]
    changes = git("diff", "--name-status", "--no-renames", base, target).splitlines()
    skipped, conflicts, merged = [], [], 0
    for line in changes:
        status, path = line.split("\t", 1)
        dest = os.path.join(port, path)
        if status == "D":
            if os.path.exists(dest):
                os.remove(dest)
            continue
        new = show(target, path)
        if status == "A":
            os.makedirs(os.path.dirname(dest), exist_ok=True)
            with open(dest, "wb") as f:
                f.write(new)
            continue
        if not os.path.exists(dest):
            skipped.append(path)
            continue
        old = show(base, path)
        with tempfile.TemporaryDirectory() as tmp:
            bp, tp = os.path.join(tmp, "base"), os.path.join(tmp, "theirs")
            with open(bp, "wb") as f:
                f.write(old)
            with open(tp, "wb") as f:
                f.write(new)
            r = subprocess.run(["git", "merge-file", "-L", "port", "-L", "base", "-L", "upstream", dest, bp, tp])
            if r.returncode != 0:
                conflicts.append(path)
            else:
                merged += 1
    print(f"merged {merged}, skipped {len(skipped)}, conflicts {len(conflicts)}")
    for p in skipped:
        print("SKIP", p)
    for p in conflicts:
        print("CONFLICT", p)


if __name__ == "__main__":
    main()
