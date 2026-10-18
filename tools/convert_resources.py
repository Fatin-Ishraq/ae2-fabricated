"""Convert NeoForge-specific JSON in the port's resources to Fabric equivalents.

Upstream commits its datagen output (src/generated). Until the data generators are ported to Fabric datagen, this
script converts the NeoForge-only constructs in place:

- "neoforge:conditions": [not(tag_empty(T))]  ->  "fabric:load_conditions": [tags_populated(T)]
- {"type": "neoforge:difference", base, subtracted}  ->  {"fabric:type": "fabric:difference", base, subtracted}
- {"neoforge:ingredient_type": "neoforge:difference", ...} (26.x)  ->  {"fabric:type": "fabric:difference", ...}
- blockstate variants {"type": "<custom model>"} (26.x NeoForge custom block state models)  ->  {"fabric:type": ...}
- biome "attributes" with "neoforge:" keys (custom sky/cloud/weather renderers, 26.x)  ->  removed
- model face "neoforge_data": {"block_light": N}  (26.x)  ->  element "light_emission": N (vanilla). Only applied
  with --vanilla-light, since the 1.21.1 port bakes these faces through its own mixins instead.

Unknown NeoForge constructs are reported and left untouched. Safe to re-run.
Usage: python convert_resources.py [resource roots...]
"""
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_ROOTS = [os.path.join(HERE, "..", "port", "src", "generated", "resources"),
                 os.path.join(HERE, "..", "port", "src", "main", "resources")]

problems = []
VANILLA_LIGHT = False


def convert_condition(cond, path):
    t = cond.get("type")
    if t == "neoforge:not":
        inner = cond.get("value", {})
        if inner.get("type") == "neoforge:tag_empty":
            return {"condition": "fabric:tags_populated", "values": [inner["tag"]]}
    if t == "neoforge:tag_empty":
        return {"condition": "fabric:not",
                "value": {"condition": "fabric:tags_populated", "values": [cond["tag"]]}}
    if t == "neoforge:mod_loaded":
        return {"condition": "fabric:all_mods_loaded", "values": [cond["modid"]]}
    problems.append(f"{path}: unsupported condition {cond}")
    return None


def convert(node, path):
    changed = False
    if isinstance(node, dict):
        if "neoforge:conditions" in node:
            conds = [convert_condition(c, path) for c in node["neoforge:conditions"]]
            if all(c is not None for c in conds):
                del node["neoforge:conditions"]
                node["fabric:load_conditions"] = conds
                changed = True
        if node.get("type") == "neoforge:difference":
            del node["type"]
            node["fabric:type"] = "fabric:difference"
            changed = True
        if node.get("neoforge:ingredient_type") == "neoforge:difference":
            del node["neoforge:ingredient_type"]
            node["fabric:type"] = "fabric:difference"
            changed = True
        attributes = node.get("attributes")
        if isinstance(attributes, dict) and any(k.startswith("neoforge:") for k in attributes):
            for k in [k for k in attributes if k.startswith("neoforge:")]:
                del attributes[k]
            changed = True
        if VANILLA_LIGHT and isinstance(node.get("faces"), dict):
            light = 0
            for face in node["faces"].values():
                data = face.pop("neoforge_data", None) if isinstance(face, dict) else None
                if data is not None:
                    light = max(light, data.get("block_light", 0))
                    changed = True
            if light > 0:
                node["light_emission"] = light
        for v in list(node.values()):
            changed |= convert(v, path)
    elif isinstance(node, list):
        for v in node:
            changed |= convert(v, path)
    return changed


def convert_blockstate(node):
    """Renames the custom block state model type key in variants and multipart entries."""
    changed = False
    if isinstance(node, dict):
        if "type" in node and "fabric:type" not in node and isinstance(node["type"], str) and ":" in node["type"]:
            node["fabric:type"] = node.pop("type")
            changed = True
        for v in list(node.values()):
            changed |= convert_blockstate(v)
    elif isinstance(node, list):
        for v in node:
            changed |= convert_blockstate(v)
    return changed


def main(roots):
    count = 0
    for root in roots:
        for dirpath, _, files in os.walk(root):
            for name in files:
                if not name.endswith(".json"):
                    continue
                path = os.path.join(dirpath, name)
                text = open(path, encoding="utf-8").read()
                is_blockstate = VANILLA_LIGHT and os.sep + "blockstates" + os.sep in os.path.normpath(path)
                if "neoforge" not in text and not (is_blockstate and '"type"' in text):
                    continue
                data = json.loads(text)
                changed = convert(data, path)
                if is_blockstate:
                    changed |= convert_blockstate(data)
                if changed:
                    with open(path, "w", encoding="utf-8", newline="\n") as f:
                        json.dump(data, f, indent=2, ensure_ascii=False)
                        f.write("\n")
                    count += 1
                # Report anything NeoForge-specific that is left
                dumped = json.dumps(data)
                if "neoforge:" in dumped and "neoforge_data" not in dumped:
                    problems.append(f"{path}: still contains neoforge: keys")
    print(f"converted {count} files")
    for p in problems:
        print("WARN", p)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--vanilla-light" in args:
        args.remove("--vanilla-light")
        VANILLA_LIGHT = True
    main(args or DEFAULT_ROOTS)
