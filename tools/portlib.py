"""Small helpers for scripted, verifiable source edits in the port tree."""
import os
import re

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "port", "src", "main", "java", "appeng")


def _p(path):
    return path if os.path.isabs(path) else os.path.join(ROOT, path)


def read(path):
    with open(_p(path), encoding="utf-8") as f:
        return f.read()


def write(path, s):
    full = _p(path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)


def sub(path, a, b, count=1):
    """Replace literal text; fails loudly if it is missing."""
    s = read(path)
    if a not in s:
        raise SystemExit(f"not found in {path}: {a!r}")
    s = s.replace(a, b) if count == 0 else s.replace(a, b, count)
    write(path, s)


def resub(path, pattern, repl, count=0, flags=re.M):
    s = read(path)
    s2, n = re.subn(pattern, repl, s, count=count, flags=flags)
    if n == 0:
        raise SystemExit(f"pattern not found in {path}: {pattern!r}")
    write(path, s2)


def replace_body(path, new_body_from_imports):
    """Replace everything from the first import line onward."""
    s = read(path)
    i = s.index("\nimport ") + 1
    write(path, s[:i] + new_body_from_imports)
