"""Show javac errors from a Gradle log, optionally filtered by file substring.

Usage: python errs.py <log> [substring] [--summary]
"""
import re
import sys

log = open(sys.argv[1], encoding="utf-8", errors="replace").read().splitlines()
flt = sys.argv[2] if len(sys.argv) > 2 and not sys.argv[2].startswith("--") else None
summary = "--summary" in sys.argv
pat = re.compile(r"^(.*?java.(appeng.*?[.]java)):([0-9]+): error: (.*)$")
i = 0
while i < len(log):
    m = pat.match(log[i])
    if m:
        rel = m.group(2).replace("\\", "/")
        block = [f"{rel}:{m.group(3)}: {m.group(4)}"]
        j = i + 1
        while j < len(log) and not pat.match(log[j]) and not log[j].startswith("Note:") and j - i < 6:
            if log[j].strip() and not log[j].strip() == "^":
                block.append("    " + log[j].strip())
            j += 1
        if flt is None or flt in rel:
            print(block[0] if summary else "\n".join(block))
        i = j
    else:
        i += 1
