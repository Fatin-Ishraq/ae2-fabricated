"""Rewrite NeoForge `<recv>.getCapability(CAP, args...)` block-capability calls to Fabric `CAP.find(recv, args...)`.

Only rewrites calls whose first argument is a known capability expression. Also maps NeoForge's built-in
capabilities to their Fabric equivalents. Prints a summary; call sites still need a manual type review.
"""
import re
import sys

CAP_MAP = {
    "Capabilities.ItemHandler.BLOCK": "ItemStorage.SIDED",
    "Capabilities.FluidHandler.BLOCK": "FluidStorage.SIDED",
    "Capabilities.EnergyStorage.BLOCK": "EnergyStorage.SIDED",
}


def split_args(s, start):
    """s[start] is just after '('. Returns (args, index after ')')."""
    depth, args, cur, i = 0, [], [], start
    while i < len(s):
        c = s[i]
        if c in "([{<" and not (c == "<" and not re.match(r"[A-Z]", s[i + 1:i + 2] or "")):
            depth += 1
        elif c in ")]}>" and not (c == ">" and depth == 0):
            if depth == 0 and c == ")":
                args.append("".join(cur).strip())
                return args, i + 1
            depth -= 1
        elif c == "," and depth == 0:
            args.append("".join(cur).strip())
            cur = []
            i += 1
            continue
        cur.append(c)
        i += 1
    raise ValueError("unbalanced")


RECV = r"((?:[A-Za-z_][\w]*(?:\(\))?)(?:\.[A-Za-z_][\w]*(?:\(\))?)*)\.getCapability\("

for path in sys.argv[1:]:
    s = open(path, encoding="utf-8").read()
    out, pos, n = [], 0, 0
    for m in re.finditer(RECV, s):
        if m.start() < pos:
            continue
        args, end = split_args(s, m.end())
        cap = args[0] if args else ""
        if len(args) in (3, 5) and ("Capabilities." in cap):
            cap = CAP_MAP.get(cap, cap)
            out.append(s[pos:m.start()])
            out.append(f"{cap}.find({m.group(1)}, {', '.join(args[1:])})")
            pos = end
            n += 1
    out.append(s[pos:])
    if n:
        open(path, "w", encoding="utf-8", newline="\n").write("".join(out))
        print(f"{path}: {n}")
