"""Auto-resolve merge conflict hunks that only contain import lines.

Takes the union of both sides, dropping net.neoforged imports (and imports of types removed by the port).
Usage: python resolve_imports.py <files...>
"""
import sys

DROP_PREFIXES = ("import net.neoforged.",)


def is_import_only(block):
    return all((not l.strip()) or l.startswith("import ") for l in block)


total = 0
for path in sys.argv[1:]:
    lines = open(path, encoding="utf-8").read().split("\n")
    out = []
    i = 0
    changed = 0
    while i < len(lines):
        if lines[i].startswith("<<<<<<< "):
            j = i + 1
            ours = []
            while not lines[j].startswith("======="):
                ours.append(lines[j])
                j += 1
            j += 1
            theirs = []
            while not lines[j].startswith(">>>>>>> "):
                theirs.append(lines[j])
                j += 1
            if is_import_only(ours) and is_import_only(theirs):
                seen = []
                for l in ours + theirs:
                    if l.startswith("import ") and not l.startswith(DROP_PREFIXES) and l not in seen:
                        seen.append(l)
                out.extend(seen)
                changed += 1
                i = j + 1
                continue
            out.extend(lines[i:j + 1])
            i = j + 1
            continue
        out.append(lines[i])
        i += 1
    if changed:
        open(path, "w", encoding="utf-8", newline="\n").write("\n".join(out))
        total += changed
print(f"resolved {total} import-only hunks")
