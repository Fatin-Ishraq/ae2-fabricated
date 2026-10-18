"""List 'cannot find symbol' method errors from a Gradle log, grouped by method and receiver type.

Usage: python missing.py <log> [method|class|variable]
"""
import collections
import os
import re
import sys

kind = sys.argv[2] if len(sys.argv) > 2 else "method"
log = open(sys.argv[1], encoding="utf-8", errors="replace").read().splitlines()
res = collections.defaultdict(set)
for i, line in enumerate(log):
    m = re.search(r"appeng.(.*?\.java):\d+: error: cannot find symbol", line)
    if not m:
        continue
    sym = loc = None
    for j in range(i + 1, min(i + 8, len(log))):
        s = re.search(r"symbol:\s+" + kind + r" (\w+)(\(.*\))?", log[j])
        if s:
            sym = s.group(1) + (s.group(2) or "")
        t = re.search(r"location: (?:variable \w+ of type|class|interface) ([\w.<>?, ]+)", log[j])
        if t:
            loc = t.group(1)
    if sym:
        res[(sym, loc)].add(os.path.basename(m.group(1).replace("\\", "/")))
for (s, l), fs in sorted(res.items(), key=lambda x: x[0][0]):
    print(f"{s:55} on {l}: {', '.join(sorted(fs))[:110]}")
