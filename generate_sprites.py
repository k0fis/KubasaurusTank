#!/usr/bin/env python3
"""Generate sprite assets for KubasaurusTank using Pillow."""
from PIL import Image, ImageDraw, ImageFilter
import os
import math
import random

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

# === SPLASH / BG / LOGO — inspired by idea.jpg (T-Rex on military tank, palms, mountains) ===

def _draw_mountains(draw, w, y_base, peaks, color_dark, color_light):
    """Draw a mountain range silhouette."""
    for px, py, pw in peaks:
        pts = [(px - pw, y_base), (px, py), (px + pw, y_base)]
        draw.polygon(pts, fill=color_dark)
        # Snow cap / lighter peak
        cap_h = (y_base - py) * 0.25
        cap_pts = [(px - pw * 0.2, py + cap_h), (px, py), (px + pw * 0.2, py + cap_h)]
        draw.polygon(cap_pts, fill=color_light)


def _draw_palm(draw, x, y, height, leaf_size):
    """Draw a palm tree."""
    trunk_color = (90, 60, 25)
    leaf_color = (30, 120, 30)
    leaf_color2 = (45, 140, 40)
    # Trunk (slightly curved)
    for i in range(height):
        t = i / height
        cx = x + int(math.sin(t * 1.5) * 5)
        tw = max(2, int(4 * (1 - t * 0.5)))
        draw.rectangle([cx - tw, y - i, cx + tw, y - i + 1],
                        fill=(trunk_color[0] + int(t * 20), trunk_color[1] + int(t * 10), trunk_color[2]))
    top_y = y - height
    top_x = x + int(math.sin(1.5) * 5)
    # Leaves — 5-6 drooping arcs
    for angle_deg in range(-150, 180, 60):
        a = math.radians(angle_deg)
        for t in range(leaf_size):
            frac = t / leaf_size
            lx = top_x + int(math.cos(a) * t * 1.5)
            ly = top_y + int(math.sin(a) * t * 0.5) + int(frac * frac * leaf_size * 0.6)
            lw = max(1, int((1 - frac) * 4))
            c = leaf_color if (t % 3 < 2) else leaf_color2
            draw.ellipse([lx - lw, ly - 1, lx + lw, ly + 1], fill=c)


def _draw_dino_tank(draw, cx, cy, scale=1.0):
    """Draw a detailed dino-tank (T-Rex fused with tank) — inspired by idea.jpg."""
    s = scale

    # === TANK BASE ===
    # Tracks
    track_color = (50, 50, 50)
    track_detail = (70, 70, 65)
    # Left track
    draw.rounded_rectangle([int(cx - 65*s), int(cy + 10*s), int(cx - 30*s), int(cy + 40*s)],
                            radius=int(5*s), fill=track_color, outline=(35, 35, 35))
    # Right track
    draw.rounded_rectangle([int(cx + 30*s), int(cy + 10*s), int(cx + 65*s), int(cy + 40*s)],
                            radius=int(5*s), fill=track_color, outline=(35, 35, 35))
    # Track wheels
    for tx_offset in [-55, -45, -35, 35, 45, 55]:
        wx = int(cx + tx_offset * s)
        wy = int(cy + 25 * s)
        r = int(6 * s)
        draw.ellipse([wx-r, wy-r, wx+r, wy+r], fill=track_detail, outline=(40, 40, 40))

    # Hull (main body)
    hull_color = (55, 130, 55)
    hull_dark = (40, 100, 40)
    hull_light = (70, 155, 65)
    hull_pts = [
        (int(cx - 55*s), int(cy + 15*s)),
        (int(cx - 45*s), int(cy - 5*s)),
        (int(cx + 50*s), int(cy - 5*s)),
        (int(cx + 60*s), int(cy + 15*s)),
    ]
    draw.polygon(hull_pts, fill=hull_color, outline=hull_dark)
    # Hull top highlight
    hull_top_pts = [
        (int(cx - 42*s), int(cy - 3*s)),
        (int(cx + 47*s), int(cy - 3*s)),
        (int(cx + 42*s), int(cy + 5*s)),
        (int(cx - 38*s), int(cy + 5*s)),
    ]
    draw.polygon(hull_top_pts, fill=hull_light)

    # Front armor plate
    draw.rectangle([int(cx + 45*s), int(cy - 2*s), int(cx + 58*s), int(cy + 12*s)],
                    fill=hull_dark, outline=(30, 80, 30))

    # === TURRET ===
    turret_color = (45, 115, 45)
    turret_light = (60, 135, 55)
    # Turret ring
    tr = int(18 * s)
    draw.ellipse([int(cx - 5*s) - tr, int(cy - 10*s) - tr,
                  int(cx - 5*s) + tr, int(cy - 10*s) + tr],
                  fill=turret_color, outline=hull_dark)
    # Turret highlight
    tri = int(12 * s)
    draw.ellipse([int(cx - 5*s) - tri, int(cy - 13*s) - tri,
                  int(cx - 5*s) + tri, int(cy - 13*s) + int(tri * 0.5)],
                  fill=turret_light)

    # === GUN BARREL ===
    barrel_color = (50, 100, 45)
    bx = int(cx + 15 * s)
    by = int(cy - 12 * s)
    barrel_len = int(55 * s)
    barrel_h = int(6 * s)
    draw.rectangle([bx, by - barrel_h//2, bx + barrel_len, by + barrel_h//2],
                    fill=barrel_color, outline=(35, 80, 35))
    # Muzzle brake
    draw.rectangle([bx + barrel_len - int(4*s), by - barrel_h, bx + barrel_len, by + barrel_h],
                    fill=(40, 90, 40), outline=(30, 70, 30))

    # === T-REX HEAD (on turret) ===
    head_color = (55, 140, 55)
    head_dark = (40, 110, 40)
    head_light = (70, 165, 65)

    # Neck
    neck_pts = [
        (int(cx - 15*s), int(cy - 25*s)),
        (int(cx - 25*s), int(cy - 55*s)),
        (int(cx - 15*s), int(cy - 60*s)),
        (int(cx - 5*s), int(cy - 30*s)),
    ]
    draw.polygon(neck_pts, fill=head_color, outline=head_dark)

    # Head (large jaw shape)
    head_pts = [
        (int(cx - 30*s), int(cy - 55*s)),  # back of head
        (int(cx - 28*s), int(cy - 75*s)),  # top of head
        (int(cx - 5*s), int(cy - 72*s)),   # forehead
        (int(cx + 15*s), int(cy - 62*s)),  # snout top
        (int(cx + 15*s), int(cy - 52*s)),  # snout bottom
        (int(cx - 5*s), int(cy - 48*s)),   # lower jaw back
        (int(cx - 30*s), int(cy - 50*s)),  # chin
    ]
    draw.polygon(head_pts, fill=head_color, outline=head_dark)

    # Upper jaw / snout lighter
    snout_pts = [
        (int(cx - 5*s), int(cy - 70*s)),
        (int(cx + 13*s), int(cy - 62*s)),
        (int(cx + 13*s), int(cy - 57*s)),
        (int(cx - 5*s), int(cy - 60*s)),
    ]
    draw.polygon(snout_pts, fill=head_light)

    # Eye
    eye_x, eye_y = int(cx - 12*s), int(cy - 66*s)
    er = int(5 * s)
    draw.ellipse([eye_x - er, eye_y - er, eye_x + er, eye_y + er],
                  fill=(255, 255, 180), outline=(40, 40, 20))
    # Pupil (slit)
    pr = int(3 * s)
    draw.ellipse([eye_x - pr + 1, eye_y - pr, eye_x + 1, eye_y + pr],
                  fill=(20, 20, 10))
    # Eye glow
    draw.ellipse([eye_x - 1, eye_y - 1, eye_x + 1, eye_y + 1], fill=(255, 255, 220))

    # Teeth (upper jaw)
    for i in range(6):
        tx = int(cx + (-2 + i * 3) * s)
        ty_top = int(cy - 57 * s)
        tooth_h = int((4 + (i % 2) * 2) * s)
        draw.polygon([(tx, ty_top), (tx + int(2*s), ty_top),
                       (tx + int(1*s), ty_top + tooth_h)],
                      fill=(255, 255, 240))
    # Teeth (lower jaw)
    for i in range(5):
        tx = int(cx + (-1 + i * 3) * s)
        ty_bot = int(cy - 52 * s)
        tooth_h = int((3 + (i % 2) * 2) * s)
        draw.polygon([(tx, ty_bot), (tx + int(2*s), ty_bot),
                       (tx + int(1*s), ty_bot - tooth_h)],
                      fill=(240, 240, 230))

    # Nostril
    nx, ny = int(cx + 10*s), int(cy - 64*s)
    draw.ellipse([nx, ny, nx + int(3*s), ny + int(2*s)], fill=head_dark)

    # Small arms (T-Rex style) on the hull
    arm_color = (50, 130, 50)
    # Left arm
    draw.line([int(cx - 20*s), int(cy - 2*s), int(cx - 28*s), int(cy + 8*s)],
              fill=arm_color, width=max(1, int(3*s)))
    draw.line([int(cx - 28*s), int(cy + 8*s), int(cx - 25*s), int(cy + 14*s)],
              fill=arm_color, width=max(1, int(2*s)))

    # Tail (stubby, coming off the back of the hull)
    tail_pts = [
        (int(cx - 50*s), int(cy + 2*s)),
        (int(cx - 75*s), int(cy - 5*s)),
        (int(cx - 80*s), int(cy + 3*s)),
        (int(cx - 70*s), int(cy + 12*s)),
        (int(cx - 50*s), int(cy + 12*s)),
    ]
    draw.polygon(tail_pts, fill=head_color, outline=head_dark)

    # Spines along neck/back
    for i in range(5):
        sx_spine = int(cx + (-22 + i * 4) * s)
        sy_spine = int(cy + (-55 + i * 8) * s)
        spine_h = int((6 - i * 0.5) * s)
        draw.polygon([(sx_spine - int(2*s), sy_spine),
                       (sx_spine, sy_spine - spine_h),
                       (sx_spine + int(2*s), sy_spine)],
                      fill=(40, 110, 35))


def gen_bg():
    print("Backgrounds & Logo:")
    random.seed(42)  # Reproducible

    # === MENU BACKGROUND (1024x768 tiled from 512x384) ===
    w, h = 512, 384
    img = Image.new('RGBA', (w, h), (8, 18, 8))
    draw = ImageDraw.Draw(img)

    # Gradient sky → ground
    for y in range(h):
        frac = y / h
        if frac < 0.3:
            # Sky gradient (dark blue-green to lighter)
            r = int(5 + frac * 30)
            g = int(15 + frac * 50)
            b = int(20 + frac * 20)
        else:
            # Ground gradient (military green gets darker)
            r = int(14 - (frac - 0.3) * 10)
            g = int(30 - (frac - 0.3) * 20)
            b = int(14 - (frac - 0.3) * 10)
        draw.line([(0, y), (w, y)], fill=(r, g, b))

    # Stars in sky area
    for _ in range(40):
        sx, sy = random.randint(0, w), random.randint(0, int(h * 0.25))
        brightness = random.randint(60, 150)
        draw.point((sx, sy), fill=(brightness, brightness, brightness + 30))

    # Distant mountains
    _draw_mountains(draw, w, int(h * 0.4),
                    [(80, int(h * 0.2), 70), (200, int(h * 0.15), 90),
                     (350, int(h * 0.22), 65), (450, int(h * 0.18), 80)],
                    (15, 35, 20), (25, 50, 30))

    # Closer mountains
    _draw_mountains(draw, w, int(h * 0.5),
                    [(60, int(h * 0.32), 50), (180, int(h * 0.28), 70),
                     (310, int(h * 0.35), 55), (420, int(h * 0.3), 65)],
                    (12, 28, 15), (18, 40, 22))

    # Ground texture — subtle camo pattern
    for _ in range(300):
        gx = random.randint(0, w)
        gy = random.randint(int(h * 0.5), h)
        gs = random.randint(3, 12)
        gc = random.choice([(10, 22, 10), (15, 30, 12), (8, 20, 8), (12, 25, 10)])
        draw.ellipse([gx, gy, gx + gs, gy + gs // 2], fill=gc)

    # Military grid overlay (subtle)
    for i in range(0, w, 64):
        draw.line([i, 0, i, h], fill=(15, 28, 15, 40), width=1)
    for i in range(0, h, 64):
        draw.line([0, i, w, i], fill=(15, 28, 15, 40), width=1)

    save(img, "bg_menu.png")

    # === SPLASH SCREEN (512x384) — Dino-tank hero image ===
    img = Image.new('RGBA', (512, 384), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Sky gradient
    for y in range(384):
        frac = y / 384
        if frac < 0.5:
            t = frac / 0.5
            r = int(30 + t * 40)
            g = int(60 + t * 60)
            b = int(80 + t * 30)
        else:
            t = (frac - 0.5) / 0.5
            r = int(70 - t * 50)
            g = int(120 - t * 70)
            b = int(110 - t * 80)
        draw.line([(0, y), (511, y)], fill=(r, g, b))

    # Mountains in background
    _draw_mountains(draw, 512, 200,
                    [(50, 100, 60), (150, 80, 80), (280, 110, 70),
                     (380, 75, 90), (480, 95, 55)],
                    (25, 55, 30), (40, 75, 45))

    # Ground
    for y in range(200, 384):
        frac = (y - 200) / 184
        r = int(40 + frac * 10)
        g = int(90 - frac * 30)
        b = int(30 + frac * 5)
        draw.line([(0, y), (511, y)], fill=(r, g, b))

    # Palm trees
    _draw_palm(draw, 50, 280, 80, 18)
    _draw_palm(draw, 440, 260, 90, 20)
    _draw_palm(draw, 480, 290, 60, 14)

    # THE DINO-TANK (center)
    _draw_dino_tank(draw, 256, 280, scale=1.4)

    # Muzzle flash
    fx, fy = 350, 252
    for r_flash in range(20, 0, -3):
        alpha = int(150 * (r_flash / 20))
        draw.ellipse([fx - r_flash, fy - r_flash // 2, fx + r_flash, fy + r_flash // 2],
                      fill=(255, 255, 100, alpha))
    draw.ellipse([fx - 5, fy - 3, fx + 5, fy + 3], fill=(255, 255, 255, 200))

    # Dust clouds at tracks
    for _ in range(15):
        dx = random.randint(170, 340)
        dy = random.randint(310, 350)
        dr = random.randint(8, 20)
        da = random.randint(40, 100)
        draw.ellipse([dx - dr, dy - dr // 2, dx + dr, dy + dr // 2],
                      fill=(120, 110, 80, da))

    # Border frame (military style — double line with corner brackets)
    draw.rectangle([4, 4, 507, 379], outline=(80, 160, 60), width=2)
    draw.rectangle([8, 8, 503, 375], outline=(40, 80, 30), width=1)
    # Corner brackets
    blen = 30
    for (bx, by, dx, dy) in [(10, 10, 1, 1), (501, 10, -1, 1),
                               (10, 373, 1, -1), (501, 373, -1, -1)]:
        draw.line([bx, by, bx + blen * dx, by], fill=(120, 200, 80), width=2)
        draw.line([bx, by, bx, by + blen * dy], fill=(120, 200, 80), width=2)

    save(img, "splash.png")

    # === GAME LOGO (400x120) — "KUBASAURUS TANK" styled text placeholder ===
    logo_w, logo_h = 400, 120
    img = Image.new('RGBA', (logo_w, logo_h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Big block letters "KUBASAURUS" — pixel art style
    def draw_block_letter(draw, x, y, letter_data, color, pixel_size=3):
        """Draw a letter from a pixel grid."""
        for row_idx, row in enumerate(letter_data):
            for col_idx, pixel in enumerate(row):
                if pixel:
                    px = x + col_idx * pixel_size
                    py = y + row_idx * pixel_size
                    draw.rectangle([px, py, px + pixel_size - 1, py + pixel_size - 1], fill=color)

    # Simple block letters for "KUBA" — keeping it recognizable as a logo mark
    # Draw a stylized dino-jaw silhouette as logo icon instead
    lcx, lcy = 60, 60
    # Tank silhouette
    draw.rounded_rectangle([lcx - 40, lcy + 5, lcx + 40, lcy + 25],
                            radius=3, fill=(50, 130, 50), outline=(30, 90, 30))
    draw.rectangle([lcx - 45, lcy + 18, lcx - 25, lcy + 28], fill=(55, 55, 55))
    draw.rectangle([lcx + 25, lcy + 18, lcx + 45, lcy + 28], fill=(55, 55, 55))
    # Barrel
    draw.rectangle([lcx + 15, lcy - 2, lcx + 55, lcy + 6], fill=(45, 110, 45), outline=(30, 80, 30))
    # Dino head mini
    head_pts_logo = [
        (lcx - 20, lcy + 5), (lcx - 22, lcy - 15), (lcx - 10, lcy - 20),
        (lcx + 5, lcy - 15), (lcx + 5, lcy - 5), (lcx - 10, lcy - 2), (lcx - 20, lcy)
    ]
    draw.polygon(head_pts_logo, fill=(55, 140, 55), outline=(35, 100, 35))
    # Eye
    draw.ellipse([lcx - 16, lcy - 16, lcx - 10, lcy - 10], fill=(255, 255, 180))
    draw.ellipse([lcx - 14, lcy - 14, lcx - 11, lcy - 11], fill=(20, 20, 10))
    # Teeth
    for i in range(4):
        tx_l = lcx - 8 + i * 4
        draw.polygon([(tx_l, lcy - 7), (tx_l + 2, lcy - 7), (tx_l + 1, lcy - 2)],
                      fill=(255, 255, 240))

    # Title text area (right side) — block style "KUBASAURUS" and "TANK"
    # Since we don't have a font loaded, draw colored rectangles as a title bar
    title_x = 130
    # "KUBASAURUS" — bright green bar with dark outline
    draw.rounded_rectangle([title_x, 10, title_x + 250, 50],
                            radius=5, fill=(15, 30, 12, 200), outline=(80, 180, 60))
    # "TANK" — yellow accent bar
    draw.rounded_rectangle([title_x + 30, 55, title_x + 220, 90],
                            radius=5, fill=(15, 30, 12, 200), outline=(220, 200, 40))

    # Decorative stars / military insignia
    star_x, star_y = title_x + 230, 70
    for angle in range(0, 360, 72):
        a = math.radians(angle - 90)
        a2 = math.radians(angle - 90 + 36)
        outer = 12
        inner = 5
        x1 = star_x + int(math.cos(a) * outer)
        y1 = star_y + int(math.sin(a) * outer)
        x2 = star_x + int(math.cos(a2) * inner)
        y2 = star_y + int(math.sin(a2) * inner)
        draw.line([star_x, star_y, x1, y1], fill=(220, 200, 40), width=2)
        draw.line([x1, y1, x2, y2], fill=(220, 200, 40), width=1)

    save(img, "logo.png")

    # === GAME OVER background overlay (512x384) ===
    img = Image.new('RGBA', (512, 384), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Dark red vignette
    for y in range(384):
        for x in range(0, 512, 4):
            dx = (x - 256) / 256
            dy = (y - 192) / 192
            dist = math.sqrt(dx * dx + dy * dy)
            alpha = int(min(200, dist * 180))
            draw.rectangle([x, y, x + 3, y], fill=(40, 0, 0, alpha))
    save(img, "bg_gameover.png")

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
