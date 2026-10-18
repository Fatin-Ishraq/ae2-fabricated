"""Check that the methods targeted by the port's mixins exist in the Minecraft jars (before launching the game).

Usage: python checkmixins.py <port dir> <classpath with ; separators>
Only checks method names given as method = "name" or "name(desc)". Targets in other mods are skipped.
"""
import json
import os
import re
import subprocess
import sys

port, cp = sys.argv[1], sys.argv[2]
cfg = json.load(open(os.path.join(port, "src", "main", "resources", "ae2.mixins.json")))
names = cfg["mixins"] + cfg["client"] + cfg.get("server", [])
cache = {}


def members(cls):
    if cls not in cache:
        out = subprocess.run(["javap", "-p", "-cp", cp, cls], capture_output=True, text=True).stdout
        cache[cls] = out
    return cache[cls]


for name in names:
    rel = name.replace(".", "/") + ".java"
    path = None
    for ss in ("main", "client"):
        p = os.path.join(port, "src", ss, "java", "appeng", "mixins", rel)
        if os.path.isfile(p):
            path = p
    if path is None:
        print(f"MISSING FILE {name}")
        continue
    src = open(path, encoding="utf-8").read()
    m = re.search(r"@Mixin\((?:value\s*=\s*)?\{?\s*(\w+)\.class", src)
    if not m:
        m2 = re.search(r'@Mixin\(targets\s*=\s*"([^"]+)"', src)
        if m2:
            print(f"skip {name} (string target {m2.group(1)})")
        continue
    simple = m.group(1)
    imp = re.search(r"import ([\w.]+\." + simple + r");", src)
    fq = imp.group(1) if imp else None
    if fq is None or not fq.startswith("net.minecraft") and not fq.startswith("com.mojang"):
        continue
    out = members(fq)
    if not out:
        print(f"{name}: class not found {fq}")
        continue
    for target in re.findall(r'method\s*=\s*"([^"]+)"', src):
        mname = re.sub(r"\(.*", "", target.split(";")[-1] if target.startswith("L") else target)
        if mname in ("<init>", "<clinit>"):
            continue
        if not re.search(r"[\s.]" + re.escape(mname) + r"\(", out):
            print(f"{name}: method {mname} not in {fq}")
print("checked", len(names), "mixins")
