"""Replay the Fabric changes of the 1.21.1 port onto the same file in a 26.x port (3-way merge).

base   = upstream 1.21.1 (tag neoforge/v19.2.18)
theirs = the 1.21.1 Fabric port (port/)
ours   = the file in the 26.x port (port-<version>/src/{main,client}/java)

Before merging, base and theirs get the renames that 26.x needs anyway (ResourceLocation -> Identifier, the 1.21.1
port's platform package names -> the 26.x stand-in packages), so that those lines do not conflict. The result is
written in place with conflict markers; review every file afterwards.

Usage: python replay121.py <port-26.x dir> <appeng/relative/Path.java>...
"""
import os
import re
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..")
UPSTREAM = os.path.join(ROOT, "upstream")
PORT121 = os.path.join(ROOT, "port", "src", "main", "java")

RENAMES = [
    ("ResourceLocation", "Identifier"),
    ("appeng.platform.registries.", "appeng.platform.neoforge.registries."),
    ("appeng.platform.config.ModConfigSpec", "appeng.platform.neoforge.common.ModConfigSpec"),
    ("appeng.platform.model.ModelData", "appeng.platform.neoforge.model.data.ModelData"),
    ("appeng.platform.model.ModelProperty", "appeng.platform.neoforge.model.data.ModelProperty"),
    ("appeng.platform.network.PacketDistributor", "appeng.platform.neoforge.network.PacketDistributor"),
    ("appeng.platform.network.NeoForgeStreamCodecs", "appeng.platform.neoforge.network.codec.NeoForgeStreamCodecs"),
    ("appeng.platform.recipes.RecipeMatcher", "appeng.platform.neoforge.common.util.RecipeMatcher"),
]


def neo_rewrite(port, text):
    sys.path.insert(0, HERE)
    import neoimports

    def repl(m):
        ref = m.group(0)
        for old, new in neoimports.PREFIXES:
            if ref.startswith(old):
                candidate = new + ref[len(old):]
                if neoimports.shim_exists(port, candidate.rstrip(".")):
                    return candidate
        return ref

    return neoimports.REF.sub(repl, text)


def prepare(port, text):
    for a, b in RENAMES:
        text = text.replace(a, b)
    return neo_rewrite(port, text)


def main():
    port = sys.argv[1]
    for rel in sys.argv[2:]:
        rel = rel.replace("\\", "/")
        ours = None
        for sourceset in ("main", "client"):
            candidate = os.path.join(port, "src", sourceset, "java", rel)
            if os.path.isfile(candidate):
                ours = candidate
        theirs_path = os.path.join(PORT121, rel)
        if ours is None or not os.path.isfile(theirs_path):
            print(f"SKIP {rel} (missing in one of the trees)")
            continue
        try:
            base = subprocess.run(["git", "-C", UPSTREAM, "show", "neoforge/v19.2.18:src/main/java/" + rel],
                                  check=True, capture_output=True).stdout.decode("utf-8")
        except subprocess.CalledProcessError:
            print(f"SKIP {rel} (not in upstream 1.21.1)")
            continue
        theirs = open(theirs_path, encoding="utf-8").read()
        with tempfile.TemporaryDirectory() as tmp:
            b = os.path.join(tmp, "base")
            t = os.path.join(tmp, "theirs")
            for path, text in ((b, base), (t, theirs)):
                with open(path, "w", encoding="utf-8", newline="\n") as f:
                    f.write(prepare(port, text))
            res = subprocess.run(["git", "merge-file", "-L", "26.x", "-L", "upstream-1.21.1", "-L", "fabric-1.21.1",
                                  ours, b, t], capture_output=True)
            conflicts = res.returncode
        print(f"{'OK  ' if conflicts == 0 else f'{conflicts:3d} '} {rel}")


if __name__ == "__main__":
    main()
