# KubasaurusTank

Izometrická tanková hra pro [kofis.eu/kuba/](https://kofis.eu/kuba/) — slot **gm009**, gameId `kfs-tank`.

Hráč ovládá dino-tank (T-Rex fusion s tankem) v izometrickém světě se 4 biomy. Střílí nepřátele ve vlnách, sbírá palivo a upgrady. Roguelike loop — jedna velká mapa, nekonečné vlny.

## Ovládání

| Akce | Klávesnice | Mobil |
|------|-----------|-------|
| Pohyb | WASD / šipky | Virtual joystick (levý) |
| Zaměření věže | Myš | Virtual joystick (pravý) |
| Střelba | LMB / Space | Auto-fire |
| Přepnutí zbraně | Q | Tap weapon icon |

## Mechaniky

- **4 zbraně:** Cannon (default), MG, Rocket, Laser
- **Palivo:** ubývá 1/s idle, 3/s při pohybu. Fuel = 0 → 10s na natankování, jinak game over
- **Vlny nepřátel:** postupně těžší, boss každou 5. vlnu
- **4 typy nepřátel:** Light (rychlý), Heavy (pomalý, silný), Sniper (drží vzdálenost), Swarm (skupinový)
- **Pickupy:** Fuel, Weapon upgrade, Armor, Speed boost, Shield
- **4 biomy v 1 mapě:** Les (NW), Město (NE, destructible budovy), Poušť (SW), Led (SE, kluzký)

## Scoring

| Akce | Body |
|------|------|
| Light tank kill | 50 |
| Heavy tank kill | 150 |
| Sniper kill | 100 |
| Swarm kill | 25 |
| Fuel pickup | 10 |
| Upgrade pickup | 25 |
| Wave complete | 200 |

## Tech stack

- **Java 17 + LibGDX 1.14.0** — custom ECS (Entity-Component-System)
- **TeaVM 0.12.0** — kompilace do JS pro web
- **Izometrický pohled** — diamond tiles 64×32px, volný pohyb (float souřadnice)
- **Mapa:** 60×40 tiles, ASCII formát
- **Leaderboard:** [score.kofis.eu](https://score.kofis.eu) (kfsLeaderboard REST API)

## Struktura

```
KubasaurusTank/
├── core/src/main/java/kfs/tank/
│   ├── KfsMain.java              # Game entry point
│   ├── World.java                # ECS world + map + entity factory
│   ├── IsoUtil.java              # Iso math (world↔screen, depth sort)
│   ├── KfsConst.java             # Veškeré konstanty
│   ├── Tile.java                 # Enum tile typů
│   ├── MusicManager.java         # Sequential MP3 playback
│   ├── SoundManager.java         # Named WAV effects
│   ├── ScoreClient.java          # Leaderboard HTTP client
│   ├── ecs/                      # Entity, KfsComp, KfsSystem, KfsWorld
│   ├── comp/                     # 11 komponent
│   ├── sys/                      # 10 systémů
│   └── ui/                       # BaseScreen, MainScreen, GameScreen,
│                                 # GameOverScreen, LeaderboardScreen
├── lwjgl3/                       # Desktop launcher (1024×768)
├── teavm/                        # Web launcher + index.html
├── assets/
│   ├── maps/arena.txt            # 60×40 mapa se 4 biomy
│   ├── sounds/                   # 8 WAV efektů
│   ├── music/                    # 3 MP3 tracky
│   ├── textures/                 # Generované PNG sprity
│   ├── fonts/                    # Press Start 2P (10/16/32pt)
│   └── game.properties           # gameId=kfs-tank
├── generate_sounds.py            # Generátor zvuků (wave module)
├── generate_music.py             # Generátor hudby (wave module → MP3)
├── generate_sprites.py           # Generátor spritů (Pillow)
├── deploy-kfstank.sh             # Deploy script pro kofis.eu
└── .github/workflows/build.yml   # CI: TeaVM build → deploy gm009
```

## Spuštění

```bash
# Desktop
./gradlew lwjgl3:run

# Web (lokální Jetty)
./gradlew teavm:run

# Web build
./gradlew teavm:build
```

## Regenerace assetů

```bash
python3 generate_sounds.py
python3 generate_music.py
# Konverze WAV → MP3:
for f in assets/music/*.wav; do ffmpeg -i "$f" -b:a 128k "${f%.wav}.mp3" && rm "$f"; done
python3 generate_sprites.py
```

## Deploy

Push na `main` → GitHub Actions → TeaVM build → deploy na `kofis.eu/kuba/gm009/`

Manuálně: `./deploy-kfstank.sh`
