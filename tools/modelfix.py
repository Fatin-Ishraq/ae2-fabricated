"""Rewrite calls to NeoForge BakedModel extension methods into appeng.platform.model.BakedModels helpers.

- recv.getQuads(state, side, rand, data, renderType)  -> BakedModels.getQuads(recv, ...)
- recv.getRenderTypes(state, rand, data)              -> BakedModels.getRenderTypes(recv, ...)
- recv.applyTransform(ctx, poseStack, leftHand)       -> BakedModels.applyTransform(recv, ...)
Calls on `this`/`super` or without receiver are left alone. Also drops @Override on NeoForge-only overrides.
Usage: python modelfix.py <files...>
"""
import re
import sys

sys.path.insert(0, __file__.rsplit("\\", 1)[0] if "\\" in __file__ else __file__.rsplit("/", 1)[0])
from globalfix import split_args  # noqa: E402

TARGETS = {"getQuads": 5, "getRenderTypes": 3, "applyTransform": 3}
RECV = r"([A-Za-z_][\w]*(?:\(\))?(?:\.[A-Za-z_][\w]*(?:\(\))?)*)\.(getQuads|getRenderTypes|applyTransform)\("
NEO_ONLY_OVERRIDES = ["applyTransform", "getRenderPasses"]


def rewrite(s):
    out, pos, n = [], 0, 0
    for m in re.finditer(RECV, s):
        if m.start() < pos:
            continue
        recv, name = m.group(1), m.group(2)
        if recv in ("this", "super") or recv.endswith(".this"):
            continue
        args, end = split_args(s, m.end())
        if len(args) != TARGETS[name]:
            continue
        out.append(s[pos:m.start()])
        out.append(f"BakedModels.{name}({recv}, {', '.join(args)})")
        pos = end
        n += 1
    out.append(s[pos:])
    s = "".join(out)
    for name in NEO_ONLY_OVERRIDES:
        s = re.sub(r"(\n[ \t]*)@Override(\s*\n[ \t]*public [^\n(]*\b" + name + r"\()", r"\2", s)
    if n and "import appeng.platform.model.BakedModels;" not in s:
        s = s.replace("\nimport appeng.", "\nimport appeng.platform.model.BakedModels;\nimport appeng.", 1)
    s = s.replace("import net.neoforged.neoforge.client.model.BakedModelWrapper;",
                  "import appeng.platform.model.BakedModelWrapper;")
    return s


if __name__ == "__main__":
    for path in sys.argv[1:]:
        s = open(path, encoding="utf-8").read()
        s2 = rewrite(s)
        if s2 != s:
            open(path, "w", encoding="utf-8", newline="\n").write(s2)
            print("modelfix", path)
