# LiquidESP

Client-side ESP overlay for lava sources in Minecraft 1.21.4 & 26.3 (Fabric). Renders semi-transparent markers through blocks to help locate lava underground.

## Features

- X-Ray style rendering through opaque blocks
- Configurable scan radius (8–64 blocks)
- Adjustable marker color with alpha transparency
- Performance-friendly: cached block positions, batched rendering, single draw call

## Configuration

Open **Mods → LiquidESP → Configure** via ModMenu.

| Parameter | Default | Description |
|-----------|---------|-------------|
| Enabled | `false` | Toggle the overlay on/off |
| Scan Radius | `24` | Block search radius. |
| Update Interval | `500` ms | How often the block cache refreshes |
| Marker Size | `1.0` | Size of the overlay cube (0.1–1.0) |
| Marker Color | `0x59FF4D00` | ARGB color with transparency |

## Building from Source

```bash
git clone https://github.com/Alexresh/LiquidESP.git
cd liquidesp
./gradlew build
```

Output jar will be in `build/libs/`.
