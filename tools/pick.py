"""Resolve conflict hunks in a file by picking a side.

Usage: python pick.py ours|theirs <file> [hunk numbers, 1-based; default all]
"""
import sys

side, path = sys.argv[1], sys.argv[2]
which = set(int(x) for x in sys.argv[3:])
lines = open(path, encoding="utf-8").read().split("\n")
out, i, n = [], 0, 0
while i < len(lines):
    if lines[i].startswith("<<<<<<< "):
        n += 1
        j = i + 1
        ours = []
        while not lines[j].startswith("======="):
            ours.append(lines[j]); j += 1
        j += 1
        theirs = []
        while not lines[j].startswith(">>>>>>> "):
            theirs.append(lines[j]); j += 1
        if not which or n in which:
            out.extend(ours if side == "ours" else theirs)
        else:
            out.extend(lines[i:j + 1])
        i = j + 1
        continue
    out.append(lines[i]); i += 1
open(path, "w", encoding="utf-8", newline="\n").write("\n".join(out))
