#!/usr/bin/env python3
"""Generate sprite assets for KubasaurusTank using Pillow."""
from PIL import Image, ImageDraw
import os
import math

OUTPUT_DIR = "assets/textures"
TILE_W, TILE_H = 64, 32

def ensure_dir():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

def save(img, name):
    path = os.path.join(OUTPUT_DIR, name)
    img.save(path)
    print(f"  {name} ({img.width}x{img.height})")

def diamond(w, h, color, outline=None):
    """Create a diamond-shaped tile."""
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    points = [(w//2, 0), (w, h//2), (w//2, h), (0, h//2)]
    draw.polygon(points, fill=color, outline=outline)
    return img

def diamond_tile(color, highlight=None, outline_col=(40, 40, 40)):
    """Tile with slight top highlight for 3D look."""
    img = diamond(TILE_W, TILE_H, color, outline_col)
    if highlight:
        draw = ImageDraw.Draw(img)
        # Top half lighter
        top_pts = [(TILE_W//2, 1), (TILE_W-4, TILE_H//2-1), (TILE_W//2, TILE_H//2-1), (4, TILE_H//2-1)]
        draw.polygon(top_pts, fill=highlight)
    return img

# === TILES ===
def gen_tiles():
    print("Tiles:")
    # Grass
    save(diamond_tile((60, 140, 40), (80, 170, 60)), "tile_grass.png")
    # Tree/Rock (darker, taller look)
    img = Image.new('RGBA', (TILE_W, TILE_H + 24), (0, 0, 0, 0))
    base = diamond_tile((25, 80, 20), (35, 100, 30))
    img.paste(base, (0, 24), base)
    draw = ImageDraw.Draw(img)
    # Tree trunk
    draw.rectangle([TILE_W//2-3, 8, TILE_W//2+3, 28], fill=(100, 60, 30))
    # Tree crown (circle)
    draw.ellipse([TILE_W//2-12, 0, TILE_W//2+12, 20], fill=(20, 100, 20), outline=(15, 70, 15))
    save(img, "tile_tree.png")
    # Water
    save(diamond_tile((20, 60, 160), (30, 80, 180), (15, 50, 140)), "tile_water.png")
    # Sand
    save(diamond_tile((200, 180, 100), (220, 200, 120)), "tile_sand.png")
    # Ice
    save(diamond_tile((170, 210, 240), (200, 230, 255), (140, 180, 210)), "tile_ice.png")
    # Road
    save(diamond_tile((90, 90, 90), (110, 110, 110), (60, 60, 60)), "tile_road.png")
    # Building
    img = Image.new('RGBA', (TILE_W, TILE_H + 32), (0, 0, 0, 0))
    base = diamond_tile((120, 100, 80), (140, 120, 95))
    img.paste(base, (0, 32), base)
    draw = ImageDraw.Draw(img)
    # Building box on top
    draw.rectangle([TILE_W//2-14, 4, TILE_W//2+14, 34], fill=(130, 110, 85), outline=(80, 70, 55))
    draw.rectangle([TILE_W//2-8, 14, TILE_W//2-2, 28], fill=(60, 80, 120))  # window
    draw.rectangle([TILE_W//2+2, 14, TILE_W//2+8, 28], fill=(60, 80, 120))  # window
    save(img, "tile_building.png")
    # Wall
    img = Image.new('RGBA', (TILE_W, TILE_H + 20), (0, 0, 0, 0))
    base = diamond_tile((110, 110, 110), (130, 130, 130), (80, 80, 80))
    img.paste(base, (0, 20), base)
    draw = ImageDraw.Draw(img)
    draw.rectangle([TILE_W//2-16, 4, TILE_W//2+16, 22], fill=(120, 120, 120), outline=(90, 90, 90))
    save(img, "tile_wall.png")
    # Fuel depot
    tile = diamond_tile((60, 140, 40), (80, 170, 60))
    draw = ImageDraw.Draw(tile)
    draw.rectangle([TILE_W//2-6, TILE_H//2-5, TILE_W//2+6, TILE_H//2+5], fill=(200, 40, 40), outline=(160, 30, 30))
    draw.text((TILE_W//2-3, TILE_H//2-4), "F", fill=(255, 255, 255))
    save(tile, "tile_fuel.png")
    # Upgrade crate
    tile = diamond_tile((60, 140, 40), (80, 170, 60))
    draw = ImageDraw.Draw(tile)
    draw.rectangle([TILE_W//2-6, TILE_H//2-5, TILE_W//2+6, TILE_H//2+5], fill=(40, 120, 200), outline=(30, 90, 160))
    draw.text((TILE_W//2-3, TILE_H//2-4), "U", fill=(255, 255, 255))
    save(tile, "tile_upgrade.png")

# === TANK ===
def gen_tank(name, body_color, turret_color, size=28):
    """Generate tank body and turret sprites (top-down view for iso rotation)."""
    print(f"Tank ({name}):")
    s = size
    # Body (rectangle with tracks)
    img = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Tracks
    draw.rectangle([2, 2, 6, s-3], fill=(60, 60, 60))
    draw.rectangle([s-7, 2, s-3, s-3], fill=(60, 60, 60))
    # Body
    draw.rectangle([5, 3, s-6, s-4], fill=body_color, outline=(body_color[0]//2, body_color[1]//2, body_color[2]//2))
    # Detail lines
    mid = s // 2
    draw.line([8, mid, s-9, mid], fill=(body_color[0]+30, body_color[1]+30, body_color[2]+30), width=1)
    save(img, f"tank_{name}_body.png")

    # Turret (circle + barrel)
    img = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Barrel
    draw.rectangle([mid, mid-2, s-2, mid+2], fill=turret_color, outline=(turret_color[0]//2, turret_color[1]//2, turret_color[2]//2))
    # Dome
    r = s // 5
    draw.ellipse([mid-r, mid-r, mid+r, mid+r], fill=turret_color, outline=(turret_color[0]*3//4, turret_color[1]*3//4, turret_color[2]*3//4))
    save(img, f"tank_{name}_turret.png")

def gen_tanks():
    gen_tank("player", (50, 160, 50), (30, 130, 30), 32)
    gen_tank("light", (200, 50, 50), (170, 30, 30), 24)
    gen_tank("heavy", (80, 80, 80), (60, 60, 60), 36)
    gen_tank("sniper", (140, 50, 180), (110, 30, 150), 26)
    gen_tank("swarm", (220, 140, 30), (190, 110, 20), 20)
    gen_tank("boss", (200, 30, 200), (170, 20, 170), 40)

# === PROJECTILES ===
def gen_projectiles():
    print("Projectiles:")
    # Cannon ball
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse([1, 1, 7, 7], fill=(255, 220, 50))
    save(img, "bullet_cannon.png")
    # MG bullet
    img = Image.new('RGBA', (6, 6), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse([1, 1, 5, 5], fill=(255, 255, 200))
    save(img, "bullet_mg.png")
    # Rocket
    img = Image.new('RGBA', (12, 6), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 1, 8, 5], fill=(200, 50, 50))
    draw.polygon([(8, 0), (12, 3), (8, 6)], fill=(255, 100, 50))
    save(img, "bullet_rocket.png")
    # Laser
    img = Image.new('RGBA', (16, 4), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 1, 16, 3], fill=(50, 200, 255))
    save(img, "bullet_laser.png")
    # Enemy bullet
    img = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse([1, 1, 7, 7], fill=(255, 60, 60))
    save(img, "bullet_enemy.png")

# === PICKUPS ===
def gen_pickups():
    print("Pickups:")
    size = 24
    def pickup_base(color, letter):
        img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        draw.rounded_rectangle([2, 2, size-3, size-3], radius=4, fill=color, outline=(255, 255, 255))
        draw.text((size//2-3, size//2-5), letter, fill=(255, 255, 255))
        return img
    save(pickup_base((200, 40, 40), "F"), "pickup_fuel.png")
    save(pickup_base((40, 150, 220), "W"), "pickup_weapon.png")
    save(pickup_base((120, 120, 120), "A"), "pickup_armor.png")
    save(pickup_base((220, 200, 30), "S"), "pickup_speed.png")
    save(pickup_base((40, 40, 220), "X"), "pickup_shield.png")

# === EXPLOSION ===
def gen_explosions():
    print("Explosions:")
    for i, (size, color) in enumerate([
        (24, (255, 200, 50)),
        (32, (255, 150, 30)),
        (40, (255, 100, 20)),
        (36, (200, 80, 20)),
        (28, (150, 60, 20)),
    ]):
        img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        r = size // 2 - 2
        cx, cy = size // 2, size // 2
        # Outer glow
        draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=(*color, 180))
        # Inner bright
        ri = r * 2 // 3
        draw.ellipse([cx-ri, cy-ri, cx+ri, cy+ri], fill=(255, 255, 200, 220))
        save(img, f"explosion_{i}.png")

# === UI ===
def gen_ui():
    print("UI elements:")
    # Crosshair
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cx, cy = 16, 16
    draw.line([cx, 2, cx, 12], fill=(255, 50, 50, 200), width=2)
    draw.line([cx, 20, cx, 30], fill=(255, 50, 50, 200), width=2)
    draw.line([2, cy, 12, cy], fill=(255, 50, 50, 200), width=2)
    draw.line([20, cy, 30, cy], fill=(255, 50, 50, 200), width=2)
    draw.ellipse([cx-4, cy-4, cx+4, cy+4], outline=(255, 50, 50, 200))
    save(img, "crosshair.png")

    # Minimap frame
    img = Image.new('RGBA', (160, 100), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 0, 159, 99], fill=(0, 0, 0, 150), outline=(100, 100, 100))
    save(img, "minimap_frame.png")

    # Weapon icons
    for name, color in [("cannon", (200, 180, 50)), ("mg", (200, 200, 200)),
                         ("rocket", (200, 60, 60)), ("laser", (60, 180, 255))]:
        img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        draw.rounded_rectangle([2, 2, 30, 30], radius=4, fill=(40, 40, 40, 200), outline=color)
        draw.text((8, 10), name[0].upper(), fill=color)
        save(img, f"icon_{name}.png")

# === SPLASH / BG ===
def gen_bg():
    print("Backgrounds:")
    # Simple dark military green background
    img = Image.new('RGBA', (256, 256), (15, 30, 15))
    draw = ImageDraw.Draw(img)
    # Subtle grid pattern
    for i in range(0, 256, 32):
        draw.line([i, 0, i, 255], fill=(20, 35, 20), width=1)
        draw.line([0, i, 255, i], fill=(20, 35, 20), width=1)
    save(img, "bg_menu.png")

    # Splash screen (simple title card)
    img = Image.new('RGBA', (512, 384), (10, 20, 10))
    draw = ImageDraw.Draw(img)
    # Border
    draw.rectangle([10, 10, 501, 373], outline=(60, 140, 40), width=3)
    # Simple dino-tank silhouette (crude but recognizable)
    cx, cy = 256, 180
    # Tank body
    draw.rectangle([cx-50, cy, cx+50, cy+30], fill=(50, 130, 50), outline=(30, 100, 30))
    # Tracks
    draw.rectangle([cx-55, cy+25, cx-35, cy+35], fill=(60, 60, 60))
    draw.rectangle([cx+35, cy+25, cx+55, cy+35], fill=(60, 60, 60))
    # Turret
    draw.ellipse([cx-15, cy-10, cx+15, cy+15], fill=(40, 110, 40))
    draw.rectangle([cx+10, cy-3, cx+60, cy+5], fill=(40, 110, 40))
    # Dino head on turret
    draw.ellipse([cx-25, cy-40, cx+5, cy-5], fill=(60, 150, 60))  # head
    draw.ellipse([cx-18, cy-35, cx-10, cy-27], fill=(255, 255, 200))  # eye
    draw.ellipse([cx-15, cy-33, cx-12, cy-30], fill=(0, 0, 0))  # pupil
    # Teeth
    for tx in range(cx-22, cx-5, 4):
        draw.polygon([(tx, cy-12), (tx+3, cy-12), (tx+1, cy-7)], fill=(255, 255, 255))
    save(img, "splash.png")

if __name__ == "__main__":
    ensure_dir()
    print("Generating KubasaurusTank sprites...\n")
    gen_tiles()
    gen_tanks()
    gen_projectiles()
    gen_pickups()
    gen_explosions()
    gen_ui()
    gen_bg()
    print("\nDone!")
