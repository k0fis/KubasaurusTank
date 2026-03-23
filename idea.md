# 🛠️ Tank Game – Design Document

## 1. Overview

**Working Title:** Steel Fury
**Genre:** Top-down / Isometric Tank Shooter
**Engine Target:** LibGDX (primary), Unity (fallback)

This game combines retro tank shooter gameplay (arcade style) with modern mechanics such as upgrades, resource management, and semi-open maps.

---

## 2. Visual Perspective

### 🎮 Camera Style

We aim for a **fake 3D look using 2D assets**:

* Isometric or angled top-down (like StarCraft)
* Not true 3D — only illusion via sprites
* Fixed camera

### 📐 Options

#### Option A: Classic Top-Down

* Easier to implement
* Fully supported in LibGDX

#### Option B: Isometric (Recommended)

* Gives depth and modern feel
* Slightly more complex math

---

## 3. Technology Choice

### ✅ LibGDX Feasibility

LibGDX **can handle this well**:

* Tile maps (Tiled editor support)
* Sprite batching (efficient rendering)
* Input handling
* Collision detection

👉 Isometric maps are possible using:

* `IsometricTiledMapRenderer`

### ⚠️ Potential Complexity

* Coordinate conversion (world ↔ tile)
* Depth sorting (render order)

### 🟡 When to switch to Unity

Switch if you need:

* Advanced lighting
* Physics-heavy gameplay
* Faster prototyping with editor tools

---

## 4. Core Gameplay Loop

1. Spawn into map
2. Explore environment
3. Engage enemies
4. Collect upgrades / fuel
5. Survive or complete objective
6. Extract or die → repeat (roguelike loop)

---

## 5. Core Systems

### 🚜 Player Tank

Attributes:

* Health
* Armor
* Fuel
* Speed
* Weapon slots

Controls:

* Movement (WASD / analog)
* Aim (mouse or direction)
* Fire

---

### 🔫 Combat System

Weapons:

* Cannon (default)
* Machine gun
* Rocket launcher
* Laser

Mechanics:

* Projectile-based shooting
* Collision detection
* Damage system

---

### ⛽ Fuel System

* Constant fuel drain over time
* Extra drain when moving
* Refuel pickups on map

Gameplay impact:

* Forces decision-making
* Prevents endless roaming

---

### 🧩 Upgrade System

Pickups on map:

* Weapon upgrades
* Armor modules
* Special abilities

Examples:

* Ricochet shots
* Shield generator
* Speed boost

---

### 👾 Enemies

Types:

* Light tank (fast)
* Heavy tank (slow, strong)
* Sniper tank (long range)
* Swarm units

AI behaviors:

* Chase player
* Keep distance
* Patrol areas

---

## 6. Map Design

### 🗺️ Structure

* Tile-based map
* Semi-open world
* Multiple regions

### 🌍 Biomes

* City (cover, destructible walls)
* Desert (open combat)
* Ice (slippery movement)

### 📦 Objects

* Obstacles
* Fuel stations
* Upgrade crates

---

## 7. Rendering Approach (LibGDX)

### 🧱 Tile Map

Use **Tiled Map Editor**:

* Export `.tmx`
* Load via LibGDX

### 🖼️ Rendering

* SpriteBatch
* Separate layers:

  * Ground
  * Objects
  * Units

### 🔄 Depth Sorting (Important)

For isometric:

* Sort by Y position
* Lower Y = render later (in front)

---

## 8. Basic Architecture

### 📦 Suggested Structure

```
core/
 ├── GameScreen
 ├── Player
 ├── Enemy
 ├── MapManager
 ├── CombatSystem
 ├── UpgradeSystem
 └── FuelSystem
```

---

## 9. MVP Scope (Start Simple)

### 🎯 Minimal Version

* One map
* Player tank
* 2 enemy types
* Shooting
* Basic collision
* Fuel system

### ➕ Then Add

* Upgrades
* More enemies
* Map variety
* UI

---

## 10. Risks & Recommendations

### ⚠️ Risks

* Isometric math complexity
* Feature creep
* Overengineering early

### ✅ Recommendations

* Start with **top-down prototype**
* Then upgrade to isometric
* Build small, test early

---

## 11. Next Steps

1. Choose perspective (top-down vs isometric)
2. Create simple prototype in LibGDX
3. Implement movement + shooting
4. Add enemies
5. Add fuel system

---

## 12. Optional Enhancements

* Procedural map generation
* Multiplayer
* Boss fights
* Meta progression

---

## Conclusion

This project is **fully feasible in LibGDX**, including isometric view.
Start simple, validate gameplay, then scale complexity.

---

If needed, next step can be:

* LibGDX starter code
* Isometric math explanation
* Player movement implementation


