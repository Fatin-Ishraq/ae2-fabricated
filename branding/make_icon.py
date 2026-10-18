"""Builds the project icon: the AE2 logo with a "Fabricated" badge.

The AE2 logo is (c) Team AppliedEnergistics, CC BY-NC-SA 3.0; this derivative is shared under the same license.
Usage: python branding/make_icon.py
Writes branding/icon.png (the listing icon) and copies it to each port's in-game logo.png. The original AE2 logo is
kept in branding/ae2-logo.png.
"""
import os

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
LOGO = os.path.join(HERE, "ae2-logo.png")
PORTS = ["port", "port-26.1.2", "port-26.2", "port-26.3"]
SIZE = 640

# Dark outline that matches the logo's black edges
OUTLINE = (30, 30, 34, 255)

RED = (214, 48, 49, 255)
RED_LIGHT = (235, 98, 95, 255)
RED_DARK = (160, 28, 32, 255)
WHITE = (250, 247, 240, 255)

FONT = "C:/Windows/Fonts/bahnschrift.ttf"


def ribbon(fill, ink, edge=None):
    """Diagonal ribbon across the bottom-right corner reading FABRICATED."""
    layer = Image.new("RGBA", (SIZE * 2, SIZE * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    c = SIZE  # centre of the oversized layer; rotation keeps it in place
    band = 92
    d.rectangle([0, c - band // 2 - 7, SIZE * 2, c + band // 2 + 7], fill=OUTLINE)
    d.rectangle([0, c - band // 2, SIZE * 2, c + band // 2], fill=fill)
    if edge:
        # Thin highlight and shadow stripes along the band, like a folded ribbon
        d.rectangle([0, c - band // 2, SIZE * 2, c - band // 2 + 5], fill=edge[0])
        d.rectangle([0, c + band // 2 - 5, SIZE * 2, c + band // 2], fill=edge[1])
    font = ImageFont.truetype(FONT, 60)
    font.set_variation_by_name("Bold")
    text = "FABRICATED"
    w = d.textlength(text, font=font)
    d.text((c - w / 2, c - 37), text, font=font, fill=ink)
    layer = layer.rotate(45, resample=Image.BICUBIC)
    # Crop so the ribbon's centre lands at (corner, corner)
    corner = int(SIZE * 0.80)
    left = c - corner
    return layer.crop((left, left, left + SIZE, left + SIZE))


def compose(overlay, shrink_logo=1.0):
    base = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    logo = Image.open(LOGO).convert("RGBA")
    if shrink_logo != 1.0:
        s = int(SIZE * shrink_logo)
        logo = logo.resize((s, s), Image.LANCZOS)
        base.alpha_composite(logo, ((SIZE - s) // 2, 0))
    else:
        base.alpha_composite(logo)
    base.alpha_composite(overlay)
    return base


def main():
    icon = compose(ribbon(RED, WHITE, (RED_LIGHT, RED_DARK)))
    icon.save(os.path.join(HERE, "icon.png"))
    for port in PORTS:
        icon.save(os.path.join(HERE, "..", port, "src", "main", "resources", "logo.png"))


if __name__ == "__main__":
    main()
