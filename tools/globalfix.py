"""Mechanical NeoForge -> Fabric rewrites that are safe to apply to any upstream file.

Usage: python globalfix.py <files...>
Re-run this on files restored from upstream so they match the rest of the port tree.
"""
import re
import sys

SIMPLE = [
    ("import net.neoforged.api.distmarker.Dist;", "import net.fabricmc.api.EnvType;"),
    ("import net.neoforged.api.distmarker.OnlyIn;", "import net.fabricmc.api.Environment;"),
    ("@OnlyIn(Dist.CLIENT)", "@Environment(EnvType.CLIENT)"),
    ("@OnlyIn(Dist.DEDICATED_SERVER)", "@Environment(EnvType.SERVER)"),
    ("net.neoforged.neoforge.registries.Deferred", "appeng.platform.registries.Deferred"),
    ("import net.neoforged.neoforge.client.model.data.ModelData;", "import appeng.platform.model.ModelData;"),
    ("import net.neoforged.neoforge.client.model.data.ModelProperty;", "import appeng.platform.model.ModelProperty;"),
    ("import net.neoforged.neoforge.network.PacketDistributor;", "import appeng.platform.network.PacketDistributor;"),
    ("import net.neoforged.neoforge.client.model.IDynamicBakedModel;", "import appeng.platform.model.IDynamicBakedModel;"),
    ("import net.neoforged.neoforge.client.ChunkRenderTypeSet;", "import appeng.platform.model.ChunkRenderTypeSet;"),
    ("MEStorage.SIDED", "AECapabilities.ME_STORAGE"),
    ("ICraftingMachine.SIDED", "AECapabilities.CRAFTING_MACHINE"),
    ("GenericInternalInventory.SIDED", "AECapabilities.GENERIC_INTERNAL_INV"),
    ("ICrankable.LOOKUP", "AECapabilities.CRANKABLE"),
    ("IInWorldGridNodeHost.LOOKUP", "AECapabilities.IN_WORLD_GRID_NODE_HOST"),
]

CAP_MAP = {
    "Capabilities.ItemHandler.BLOCK": "ItemStorage.SIDED",
    "Capabilities.FluidHandler.BLOCK": "FluidStorage.SIDED",
    "Capabilities.EnergyStorage.BLOCK": "EnergyStorage.SIDED",
}


def split_args(s, start):
    depth, args, cur, i = 0, [], [], start
    while i < len(s):
        c = s[i]
        if c in "([{":
            depth += 1
        elif c in ")]}":
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


def rewrite_get_capability(s):
    out, pos = [], 0
    for m in re.finditer(RECV, s):
        if m.start() < pos:
            continue
        args, end = split_args(s, m.end())
        cap = args[0] if args else ""
        if len(args) in (3, 5) and "Capabilities." in cap:
            cap = CAP_MAP.get(cap, cap)
            out.append(s[pos:m.start()])
            out.append(f"{cap}.find({m.group(1)}, {', '.join(args[1:])})")
            pos = end
    out.append(s[pos:])
    return "".join(out)


def fix(s):
    for a, b in SIMPLE:
        s = s.replace(a, b)
    s = rewrite_get_capability(s)
    return s


if __name__ == "__main__":
    for path in sys.argv[1:]:
        with open(path, encoding="utf-8") as f:
            s = f.read()
        s2 = fix(s)
        if s2 != s:
            with open(path, "w", encoding="utf-8", newline="\n") as f:
                f.write(s2)
            print("fixed", path)
