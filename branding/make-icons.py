#!/usr/bin/env python3
"""Regenerate every Eyemail icon from one drawing.

The source is this script plus eyemail-icon.svg. Running it writes:

  branding/app-icons/*.png      the web favicon, touch icon and manifest icons
  native/app/src/main/res/...   the Android launcher icons, in all five densities

Nothing is copied by hand, so the two surfaces can never drift apart.
Run from the repository root:

    python3 branding/make-icons.py
"""
import os
from PIL import Image, ImageDraw

ACCENT = (24, 87, 184)
ACCENT2 = (15, 61, 134)
WHITE = (255, 255, 255)

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WEB_DIR = os.path.join(ROOT, "branding", "app-icons")
ANDROID_RES = os.path.join(ROOT, "native", "app", "src", "main", "res")

WEB_SIZES = {"eyemail-512.png": 512, "eyemail-192.png": 192, "eyemail-180.png": 180,
             "eyemail-96.png": 96, "eyemail-48.png": 48, "eyemail-32.png": 32}

DENSITIES = {"mipmap-mdpi": 48, "mipmap-hdpi": 72, "mipmap-xhdpi": 96,
             "mipmap-xxhdpi": 144, "mipmap-xxxhdpi": 192}


def draw(size, rounded=True):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for y in range(size):
        t = y / max(1, size - 1)
        colour = tuple(int(ACCENT[i] + (ACCENT2[i] - ACCENT[i]) * t) for i in range(3))
        d.line([(0, y), (size, y)], fill=colour + (255,))
    if rounded:
        mask = Image.new("L", (size, size), 0)
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, size - 1, size - 1],
                                               radius=int(size * 0.22), fill=255)
        img.putalpha(mask)
    pad = size * 0.24
    box = [pad, size * 0.30, size - pad, size * 0.70]
    w = max(2, int(size * 0.045))
    d.rounded_rectangle(box, radius=int(size * 0.04), outline=WHITE + (255,), width=w)
    d.line([(box[0] + w, box[1] + size * 0.05), (size / 2, size * 0.52)],
           fill=WHITE + (255,), width=w)
    d.line([(box[2] - w, box[1] + size * 0.05), (size / 2, size * 0.52)],
           fill=WHITE + (255,), width=w)
    return img


def make_round_and_opaque(out_dir, android_mipmaps):
    """Two rules that are easy to lose on a regeneration.

    1. The round launcher icon must be a circle, not a copy of the square one, or a
       launcher that asks for the round icon shows a rounded square.
    2. The apple touch icon must have no transparency, because iOS paints a transparent
       icon on black.
    """
    from PIL import Image, ImageDraw
    master = Image.open(out_dir / "eyemail-1024-square.png").convert("RGBA")

    flat = Image.new("RGBA", master.size, (24, 87, 184, 255))
    flat.alpha_composite(master)
    flat.resize((180, 180), Image.LANCZOS).convert("RGB").save(out_dir / "eyemail-180.png")

    for density, size in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        big = size * 4
        circle = Image.new("RGBA", (big, big), (0, 0, 0, 0))
        ImageDraw.Draw(circle).ellipse((0, 0, big - 1, big - 1), fill=(24, 87, 184, 255))
        circle.alpha_composite(master.resize((big, big), Image.LANCZOS))
        mask = Image.new("L", (big, big), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, big - 1, big - 1), fill=255)
        circle.putalpha(mask)
        circle.resize((size, size), Image.LANCZOS).save(
            android_mipmaps / f"mipmap-{density}" / "ic_launcher_round.png"
        )
    print("round launcher icons and an opaque apple icon written")


def make_extra(out_dir):
    """The maskable icon and the favicon, both made from the same master."""
    from PIL import Image
    master = Image.open(out_dir / "eyemail-1024-square.png").convert("RGBA")

    # Full bleed: the master already carries its blue background, so scaling it to the
    # whole canvas leaves no seam. Android crops the shape, the logo stays inside the
    # safe zone on its own.
    size = 512
    maskable = Image.new("RGBA", (size, size), (24, 87, 184, 255))
    maskable.alpha_composite(master.resize((size, size), Image.LANCZOS))
    maskable.save(out_dir / "eyemail-512-maskable.png")

    master.resize((48, 48), Image.LANCZOS).save(out_dir / "favicon.ico", sizes=[(16, 16), (32, 32), (48, 48)])
    print("wrote eyemail-512-maskable.png and favicon.ico")


def main():
    os.makedirs(WEB_DIR, exist_ok=True)
    for name, size in WEB_SIZES.items():
        draw(size).save(os.path.join(WEB_DIR, name))
        print("web   ", name)
    draw(1024, rounded=False).save(os.path.join(WEB_DIR, "eyemail-1024-square.png"))
    print("web    eyemail-1024-square.png")

    for folder, size in DENSITIES.items():
        out = os.path.join(ANDROID_RES, folder)
        os.makedirs(out, exist_ok=True)
        icon = draw(size).convert("RGBA")
        icon.save(os.path.join(out, "ic_launcher.png"))
        icon.save(os.path.join(out, "ic_launcher_round.png"))
        print("android", folder, size)


if __name__ == "__main__":
    main()
