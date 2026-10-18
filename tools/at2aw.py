"""Convert a NeoForge accesstransformer.cfg (Mojang names) into Fabric access widener lines.

Usage: python at2aw.py <accesstransformer.cfg> <named minecraft jar>
Field descriptors are looked up with javap since ATs do not carry them.
"""
import re
import subprocess
import sys

at_path, jar = sys.argv[1], sys.argv[2]
out = []
field_cache = {}


def fields_of(owner):
    if owner not in field_cache:
        res = subprocess.run(["javap", "-p", "-s", "-cp", jar, owner.replace("/", ".")],
                             capture_output=True, text=True)
        fields = {}
        lines = res.stdout.splitlines()
        for i, line in enumerate(lines):
            m = re.match(r"\s+.*?\b([A-Za-z_$][\w$]*);$", line)
            if m and "(" not in line and i + 1 < len(lines) and "descriptor:" in lines[i + 1]:
                fields[m.group(1)] = lines[i + 1].split("descriptor:")[1].strip()
        field_cache[owner] = fields
    return field_cache[owner]


for raw in open(at_path, encoding="utf-8"):
    line = raw.split("#")[0].strip()
    if not line:
        continue
    parts = line.split()
    access, owner = parts[0], parts[1].replace(".", "/")
    final_removed = access.endswith("-f")
    if len(parts) == 2:
        out.append(f"accessible class {owner}")
        if final_removed:
            out.append(f"extendable class {owner}")
        continue
    member = parts[2]
    if "(" in member:
        name, desc = member[:member.index("(")], member[member.index("("):]
        out.append(f"accessible method {owner} {name} {desc}")
        if final_removed:
            out.append(f"extendable method {owner} {name} {desc}")
    else:
        desc = fields_of(owner).get(member)
        if desc is None:
            print(f"# WARNING: could not resolve field {owner}.{member}", file=sys.stderr)
            continue
        out.append(f"accessible field {owner} {member} {desc}")
        if final_removed:
            out.append(f"mutable field {owner} {member} {desc}")

print("\n".join(out))
