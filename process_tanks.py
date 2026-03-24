#!/usr/bin/env python3
"""Process AI-generated tank images into game sprites (body + turret)."""
from PIL import Image, ImageDraw, ImageFilter, ImageEnhance
import os
import math

IMGS_DIR = "imgs"
OUT_DIR = "assets/textures"
# Game sprite sizes
BODY_SIZE = 64  # px, matches tile width for good visibility
TURRET_SIZE = 64

# Source images → role mapping
TANKS = {
    "player": "_e978204f-ddc2-41ff-ae76-d9113d9653b9.jpg",   # cleanest iso
    "boss":   "_026037b6-8f73-4497-89dc-faf65d29b5e1.jpg",   # massive, heavy
    "heavy":  "_ff94b732-1d48-4273-9de7-e74f4def7242.jpg",    # dark, antennas
    "sniper": "_af602bf6-a738-40f7-9f92-3c003c3f39e9.jpg",    # open turret ring
}

# Color tints per role (R, G, B multipliers)
TINTS = {
    "player": (0.6, 1.2, 0.6),   # green
    "boss":   (1.3, 0.3, 1.3),   # magenta
    "heavy":  (0.7, 0.7, 0.7),   # dark gray
    "sniper": (0.9, 0.4, 1.2),   # purple
}


def remove_background(img):
    """Remove white/gray/checkered background → transparent PNG."""
    img = img.convert("RGBA")
    pixels = img.load()
    w, h = img.size

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            # White/near-white background
            if r > 210 and g > 210 and b > 210:
                pixels[x, y] = (0, 0, 0, 0)
            # Gray uniform background
            elif abs(r - g) < 15 and abs(g - b) < 15 and r > 100 and r < 170:
                pixels[x, y] = (0, 0, 0, 0)
            # Checkered transparency pattern: two alternating grays
            # Typical values: ~204,204,204 and ~255,255,255 or ~153,153,153
            elif abs(r - g) < 8 and abs(g - b) < 8 and (r > 185 or (r > 135 and r < 175)):
                pixels[x, y] = (0, 0, 0, 0)
            # Light shadow remnants (very low saturation, medium brightness)
            elif abs(r - g) < 10 and abs(g - b) < 10 and r > 170 and r < 210:
                pixels[x, y] = (0, 0, 0, 0)

    return img


def flood_fill_bg(img, tolerance=40):
    """Flood fill from edges to remove connected background."""
    img = img.convert("RGBA")
    w, h = img.size
    pixels = img.load()
    visited = set()

    # Sample from many edge points, not just corners
    edge_points = []
    # All corners
    for cx, cy in [(0, 0), (w-1, 0), (0, h-1), (w-1, h-1)]:
        edge_points.append((cx, cy))
    # Points along all 4 edges (every 20px)
    for i in range(0, w, 20):
        edge_points.extend([(i, 0), (i, h-1)])
    for i in range(0, h, 20):
        edge_points.extend([(0, i), (w-1, i)])

    for start_x, start_y in edge_points:
        if (start_x, start_y) in visited:
            continue
        bg_r, bg_g, bg_b, _ = pixels[start_x, start_y]
        stack = [(start_x, start_y)]

        while stack:
            x, y = stack.pop()
            if (x, y) in visited or x < 0 or y < 0 or x >= w or y >= h:
                continue

            r, g, b, a = pixels[x, y]
            if abs(r - bg_r) < tolerance and abs(g - bg_g) < tolerance and abs(b - bg_b) < tolerance:
                visited.add((x, y))
                pixels[x, y] = (0, 0, 0, 0)
                stack.extend([(x+1, y), (x-1, y), (x, y+1), (x, y-1)])

    return img


def crop_to_content(img, padding=4):
    """Crop to non-transparent bounding box with padding."""
    bbox = img.getbbox()
    if bbox is None:
        return img
    x1, y1, x2, y2 = bbox
    x1 = max(0, x1 - padding)
    y1 = max(0, y1 - padding)
    x2 = min(img.width, x2 + padding)
    y2 = min(img.height, y2 + padding)
    return img.crop((x1, y1, x2, y2))


def make_square(img):
    """Pad to square with transparent pixels."""
    w, h = img.size
    s = max(w, h)
    result = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    result.paste(img, ((s - w) // 2, (s - h) // 2), img)
    return result


def apply_tint(img, tint):
    """Apply color tint (R, G, B multipliers)."""
    img = img.convert("RGBA")
    pixels = img.load()
    w, h = img.size
    tr, tg, tb = tint

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a > 0:
                pixels[x, y] = (
                    min(255, int(r * tr)),
                    min(255, int(g * tg)),
                    min(255, int(b * tb)),
                    a
                )
    return img


def soften_edges(img, radius=1):
    """Soften sprite edges for cleaner look at small size."""
    # Split alpha, blur it slightly, recombine
    r, g, b, a = img.split()
    # Don't blur alpha — just return as-is for sharp edges
    return img


def generate_turret(size, tint, barrel_type="cannon"):
    """Generate a turret sprite: dome + barrel."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cx, cy = size // 2, size // 2

    tr, tg, tb = tint
    base_r, base_g, base_b = 100, 100, 100  # gray base
    # Apply tint
    cr = min(255, int(base_r * tr))
    cg = min(255, int(base_g * tg))
    cb = min(255, int(base_b * tb))
    dark_r = max(0, cr - 40)
    dark_g = max(0, cg - 40)
    dark_b = max(0, cb - 40)

    if barrel_type == "cannon":
        # Single thick cannon barrel pointing right
        bw = size // 3  # barrel length
        bh = max(3, size // 8)  # barrel thickness
        draw.rectangle(
            [cx, cy - bh, cx + bw, cy + bh],
            fill=(dark_r, dark_g, dark_b, 230),
            outline=(dark_r // 2, dark_g // 2, dark_b // 2, 255)
        )
        # Muzzle brake
        draw.rectangle(
            [cx + bw - 2, cy - bh - 1, cx + bw + 1, cy + bh + 1],
            fill=(dark_r // 2, dark_g // 2, dark_b // 2, 200)
        )
    elif barrel_type == "rocket":
        # Dual rocket pods
        bw = size // 3
        bh = max(2, size // 12)
        gap = max(3, size // 8)
        for offset in [-gap, gap]:
            draw.rectangle(
                [cx, cy + offset - bh, cx + bw, cy + offset + bh],
                fill=(dark_r, dark_g, dark_b, 230),
                outline=(dark_r // 2, dark_g // 2, dark_b // 2, 255)
            )
            # Missile tips (red)
            draw.rectangle(
                [cx + bw - 1, cy + offset - bh + 1, cx + bw + 2, cy + offset + bh - 1],
                fill=(200, 50, 50, 220)
            )
    elif barrel_type == "mg":
        # Twin MG barrels
        bw = size // 4
        bh = max(1, size // 16)
        gap = max(2, size // 10)
        for offset in [-gap, gap]:
            draw.rectangle(
                [cx + 2, cy + offset - bh, cx + bw + 2, cy + offset + bh],
                fill=(dark_r, dark_g, dark_b, 220)
            )
    elif barrel_type == "laser":
        # Single thin laser emitter
        bw = size // 3
        bh = max(1, size // 14)
        draw.rectangle(
            [cx, cy - bh, cx + bw, cy + bh],
            fill=(50, 180, 255, 200)
        )
        # Glow tip
        draw.ellipse(
            [cx + bw - 2, cy - bh - 1, cx + bw + 3, cy + bh + 1],
            fill=(100, 220, 255, 180)
        )

    # Turret dome (circle)
    dome_r = max(4, size // 5)
    draw.ellipse(
        [cx - dome_r, cy - dome_r, cx + dome_r, cy + dome_r],
        fill=(cr, cg, cb, 240),
        outline=(dark_r, dark_g, dark_b, 255)
    )
    # Dome highlight
    hr = dome_r * 2 // 3
    draw.ellipse(
        [cx - hr, cy - hr - 1, cx + hr, cy - 1],
        fill=(min(255, cr + 40), min(255, cg + 40), min(255, cb + 40), 120)
    )

    return img


def process_tank(name, filename, tint):
    """Full pipeline: load → remove bg → crop → tint → scale → save body + turrets."""
    filepath = os.path.join(IMGS_DIR, filename)
    if not os.path.exists(filepath):
        print(f"  SKIP {filename} — not found")
        return

    print(f"\n{name} ({filename}):")

    # Load and remove background
    img = Image.open(filepath)
    print(f"  Source: {img.width}x{img.height}")

    # First pass: flood fill from corners
    img = img.convert("RGBA")
    img = flood_fill_bg(img, tolerance=35)

    # Second pass: remove remaining near-white/gray pixels
    img = remove_background(img)

    # Crop to content
    img = crop_to_content(img)
    img = make_square(img)
    print(f"  After crop+square: {img.width}x{img.height}")

    # Apply tint
    tinted = apply_tint(img.copy(), tint)

    # Scale to body size
    body = tinted.resize((BODY_SIZE, BODY_SIZE), Image.LANCZOS)

    # Save body
    body_path = os.path.join(OUT_DIR, f"tank_{name}_body.png")
    body.save(body_path)
    print(f"  Body: {body_path} ({BODY_SIZE}x{BODY_SIZE})")

    # Generate turret sprites (cannon is default)
    turret = generate_turret(TURRET_SIZE, tint, "cannon")
    turret_path = os.path.join(OUT_DIR, f"tank_{name}_turret.png")
    turret.save(turret_path)
    print(f"  Turret: {turret_path} ({TURRET_SIZE}x{TURRET_SIZE})")

    # Also save un-tinted body for reference
    original_body = img.resize((BODY_SIZE, BODY_SIZE), Image.LANCZOS)
    ref_path = os.path.join(IMGS_DIR, f"ref_{name}_body.png")
    original_body.save(ref_path)
    print(f"  Reference (un-tinted): {ref_path}")


def generate_all_turret_variants():
    """Generate weapon-specific turrets for the player."""
    print("\nPlayer weapon turrets:")
    tint = TINTS["player"]
    for weapon in ["cannon", "mg", "rocket", "laser"]:
        turret = generate_turret(TURRET_SIZE, tint, weapon)
        path = os.path.join(OUT_DIR, f"turret_player_{weapon}.png")
        turret.save(path)
        print(f"  {weapon}: {path}")


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Processing AI-generated tank images into game sprites...\n")

    for name, filename in TANKS.items():
        tint = TINTS.get(name, (1.0, 1.0, 1.0))
        process_tank(name, filename, tint)

    # Also generate light/swarm tanks from the player base (recolored)
    extra = {
        "light":  ("_e978204f-ddc2-41ff-ae76-d9113d9653b9.jpg", (1.3, 0.4, 0.4)),  # red
        "swarm":  ("_e978204f-ddc2-41ff-ae76-d9113d9653b9.jpg", (1.3, 0.9, 0.2)),  # orange
    }
    for name, (filename, tint) in extra.items():
        process_tank(name, filename, tint)

    generate_all_turret_variants()

    print("\nDone! Sprites saved to", OUT_DIR)


if __name__ == "__main__":
    main()
