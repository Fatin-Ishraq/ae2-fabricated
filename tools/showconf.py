"""Print merge conflict hunks (with a little leading context) for the given files."""
import sys

ctx = 2
for path in sys.argv[1:]:
    lines = open(path, encoding="utf-8").read().split("\n")
    i = 0
    while i < len(lines):
        if lines[i].startswith("<<<<<<< "):
            start = i
            while not lines[i].startswith(">>>>>>> "):
                i += 1
            print(f"==== {path}:{start + 1}")
            for j in range(max(0, start - ctx), i + 1):
                print(lines[j])
        i += 1
