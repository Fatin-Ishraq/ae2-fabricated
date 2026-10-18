"""Rewrite NeoForge references to the port's Fabric-backed stand-ins (26.x ports).

The stand-ins live under appeng.platform and mirror the NeoForge package layout:
    net.neoforged.neoforge.X   ->  appeng.platform.neoforge.X
    net.neoforged.fml.X        ->  appeng.platform.fml.X
    net.neoforged.bus.api.X    ->  appeng.platform.bus.api.X
    net.neoforged.api.X        ->  appeng.platform.api.X
A reference is only rewritten when the stand-in class exists (in src/main/java or src/client/java), so everything
that still needs porting keeps failing to compile. Safe to re-run.

Usage: python neoimports.py <port dir> [files...]
"""
import os
import re
import sys

PREFIXES = [
    ("net.neoforged.neoforge.", "appeng.platform.neoforge."),
    ("net.neoforged.fml.", "appeng.platform.fml."),
    ("net.neoforged.bus.api.", "appeng.platform.bus.api."),
    ("net.neoforged.api.", "appeng.platform.api."),
]

REF = re.compile(r"\bnet\.neoforged\.[A-Za-z0-9_.]+")


def shim_exists(port, fqcn):
    rel = fqcn.replace(".", "/")
    for sourceset in ("main", "client"):
        base = os.path.join(port, "src", sourceset, "java")
        # Try the full name, then treat trailing segments as nested classes / static members
        parts = rel.split("/")
        for n in range(len(parts), 0, -1):
            if os.path.isfile(os.path.join(base, *parts[:n]) + ".java"):
                return True
    return False


def rewrite(port, path):
    with open(path, encoding="utf-8") as f:
        text = f.read()

    def repl(m):
        ref = m.group(0)
        for old, new in PREFIXES:
            if ref.startswith(old):
                candidate = new + ref[len(old):]
                if shim_exists(port, candidate.rstrip(".")):
                    return candidate
        return ref

    new_text = REF.sub(repl, text)
    if new_text != text:
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write(new_text)
        return True
    return False


def main():
    port = sys.argv[1]
    files = sys.argv[2:]
    if not files:
        for sourceset in ("main", "client", "test"):
            root = os.path.join(port, "src", sourceset, "java")
            for dirpath, _, names in os.walk(root):
                if os.sep + "platform" + os.sep in dirpath + os.sep and "appeng" + os.sep + "platform" in dirpath:
                    continue
                files += [os.path.join(dirpath, n) for n in names if n.endswith(".java")]
    changed = sum(rewrite(port, f) for f in files)
    print(f"rewrote {changed} files")


if __name__ == "__main__":
    main()
