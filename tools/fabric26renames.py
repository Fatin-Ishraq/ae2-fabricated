"""Rename Fabric API / vanilla names that changed between 1.21.1 and 26.x in code replayed from the 1.21.1 port.

Usage: python fabric26renames.py <files...>
"""
import re
import sys

RENAMES = [
    ("net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents", "net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents"),
    (r"\bServerWorldEvents\b", "ServerLevelEvents"),
    (r"\bSTART_WORLD_TICK\b", "START_LEVEL_TICK"),
    (r"\bEND_WORLD_TICK\b", "END_LEVEL_TICK"),
    ("net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries", "net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput"),
    ("net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup", "net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab"),
    ("net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents", "net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents"),
    (r"\bFabricItemGroupEntries\b", "FabricCreativeModeTabOutput"),
    (r"\bFabricItemGroup\b", "FabricCreativeModeTab"),
    (r"\bItemGroupEvents\.MODIFY_ENTRIES_ALL\b", "CreativeModeTabEvents.MODIFY_OUTPUT_ALL"),
    ("net.fabricmc.fabric.api.blockview.v2.", "net.fabricmc.fabric.api.blockgetter.v2."),
    ("import net.minecraft.world.ItemInteractionResult;\n", ""),
    (r"\bItemInteractionResult\b", "InteractionResult"),
    ("net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType", "net.fabricmc.fabric.api.menu.v1.ExtendedMenuType"),
    ("net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory", "net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider"),
    ("net.minecraft.client.gui.GuiGraphics;", "net.minecraft.client.gui.GuiGraphicsExtractor;"),
    (r"\bGuiGraphics\b", "GuiGraphicsExtractor"),
    ("net.minecraft.resources.ResourceLocation;", "net.minecraft.resources.Identifier;"),
    (r"\bResourceLocation\b", "Identifier"),
]

for path in sys.argv[1:]:
    s = open(path, encoding="utf-8").read()
    t = s
    for a, b in RENAMES:
        if a.startswith("\\b") or "\\" in a:
            t = re.sub(a, b, t)
        else:
            t = t.replace(a, b)
    if t != s:
        open(path, "w", encoding="utf-8", newline="\n").write(t)
        print("renamed", path)
