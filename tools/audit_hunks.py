"""Audit port changes against upstream 1.21.1.

For every Java file that differs from upstream, split the unified diff into hunks and flag hunks that do not mention
any platform-related token. Those are candidates for accidental regressions (e.g. clean merge hunks that reverted
upstream fixes).

Usage: python audit_hunks.py [--all] [path-prefix]
"""
import difflib
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
UP = os.path.join(HERE, "..", "upstream", "src", "main", "java")
PORT = os.path.join(HERE, "..", "port", "src", "main", "java")

PLATFORM = re.compile(
    r"fabric|neoforge|neoforged|Fabric|NeoForge|Storage|Variant|Transaction|transaction|Energy|energy|"
    r"[Cc]apabilit|Lookup|lookup|ModelData|RenderAttach|RenderData|Environment|EnvType|OnlyIn|Dist\b|"
    r"FluidStack|IItemHandler|IFluidHandler|FluidAction|SIDED|AECapabilities|PowerUnit|Packet|packet|"
    r"PacketDistributor|ItemStorage|FluidStorage|Event|event|Mixin|mixin|accessor|Accessor|platform|"
    r"DeferredRegister|Deferred|register|Registry|Codec|guideme|PageAnchor|Guide|import ")

show_all = "--all" in sys.argv
prefix = next((a for a in sys.argv[1:] if not a.startswith("--")), "appeng")

for root, _, files in os.walk(os.path.join(PORT, prefix)):
    for name in files:
        if not name.endswith(".java"):
            continue
        path = os.path.join(root, name)
        rel = os.path.relpath(path, PORT).replace("\\", "/")
        up = os.path.join(UP, rel)
        if not os.path.exists(up):
            continue
        a = open(up, encoding="utf-8").read().splitlines()
        b = open(path, encoding="utf-8").read().splitlines()
        if a == b:
            continue
        hunks = []
        for group in difflib.SequenceMatcher(None, a, b).get_grouped_opcodes(1):
            removed, added = [], []
            for tag, i1, i2, j1, j2 in group:
                if tag in ("replace", "delete"):
                    removed += a[i1:i2]
                if tag in ("replace", "insert"):
                    added += b[j1:j2]
            text = "\n".join(removed + added)
            if not text.strip():
                continue
            flagged = not PLATFORM.search(text)
            if flagged or show_all:
                hunks.append((group[0][1] + 1, removed, added, flagged))
        if hunks:
            print(f"=== {rel}")
            for line, removed, added, flagged in hunks:
                print(f"--- upstream line {line}{'  [FLAG]' if flagged else ''}")
                for l in removed:
                    print("-" + l)
                for l in added:
                    print("+" + l)
