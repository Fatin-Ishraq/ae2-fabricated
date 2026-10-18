# Applied Energistics 2: Fabricated

[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) for Fabric: ME storage networks,
autocrafting, P2P tunnels and spatial storage, for Minecraft 1.21.1 and 26.x.

> **This is an unofficial port.** It is not made, endorsed or supported by the Applied Energistics 2 team. Please
> report bugs to [this repository's issue tracker](https://github.com/Fatin-Ishraq/ae2-fabricated/issues), **not**
> to the AE2 team.

The AE2 team releases for NeoForge only. Fabricated ports their code to Fabric, keeping as close to upstream as
possible: same content, same recipes, same mod ID (`ae2`) and the same `appeng.api` API for addons.

## Versions

| Minecraft | Based on AE2 | Mod version | Directory |
|---|---|---|---|
| 1.21.1 | `neoforge/v19.2.18` | `19.2.18-alpha.1+fabric` | `port/` |
| 26.1.2 | `26.1.13-beta` (+1 fix from the `26.1` branch) | `26.1.13-beta.1+fabric` | `port-26.1.2/` |
| 26.2 | `v26.2.1-alpha` | `26.2.1-alpha.1+fabric` | `port-26.2/` |
| 26.3 | `v26.3.1-alpha` | `26.3.1-alpha.1+fabric` | `port-26.3/` |

All versions need [Fabric API](https://modrinth.com/mod/fabric-api). Team Reborn Energy is bundled. All releases
are **alpha**: they pass upstream's automated tests, but haven't had much real-world play yet.

## Status

- **1.21.1:** all 466 upstream unit tests and all 69 upstream game tests pass. The game tests cover ME networks,
  autocrafting, P2P, spatial IO, channels and pattern providers.
- **26.x:** each version passes 539 unit tests and all 77 game tests.
- **Client playtests:** every version has a scripted client playtest. It builds a showcase network and takes
  screenshots of it.
  - The world: cables, parts, facades, the crafting CPU, the quantum bridge and the area overlay.
  - The GUIs: terminal, crafting terminal, ME chest and drive.

### Integrations

- 1.21.1: EMI, REI, Jade and WTHIT.
- 26.x: JEI, Jade and WTHIT. Like upstream AE2, the 26.x versions have no REI or EMI integration.
- Not included: The One Probe and Curios (both NeoForge-only), and the in-game guidebook (GuideME is NeoForge-only).

### Known differences from AE2 on NeoForge

- Fluid amounts use Fabric's units (81000 droplets per bucket), so worlds can't be moved between NeoForge and Fabric.
- On 26.x, the spatial storage dimension uses the vanilla sky, clouds and weather. Fabric 26.x has no way to give a
  dimension custom sky rendering.
- No in-game config screen. Settings are in `config/ae2-client.toml` and `config/ae2-common.toml`.

See [NOTES.md](NOTES.md) for the full porting log, design decisions and known gaps.

## Building

1.21.1 needs JDK 21 and 26.x needs JDK 25. Each version is its own Gradle project; replace `port` with `port-26.1.2`,
`port-26.2` or `port-26.3` for the others.

```
cd port
./gradlew build              # mod jar in build/libs (also runs the unit tests)
./gradlew test               # unit tests
./gradlew runGametest        # headless game tests
./gradlew runClient          # dev client
./gradlew runClientGameTest  # 26.x: scripted client playtest, screenshots in build/run/clientGameTest/screenshots
./gradlew runPlaytest        # 1.21.1: scripted client playtest, screenshots in build/playtest/screenshots
                             # (run only some steps with -PplaytestSteps=showcase,area_overlay,terminal,drive,plots)
```

## Repository layout

- `port/`: the 1.21.1 port.
- `port-26.1.2/`, `port-26.2/`, `port-26.3/`: the Minecraft 26.x ports.
- `tools/`: the scripts used to port and maintain the code (merge helpers, import rewriter, resource converter). Some
  expect an upstream AE2 checkout in `upstream/` (see NOTES.md).
- `NOTES.md`: the porting log.

## Credits

Applied Energistics 2 is made by AlgorithmX2 and Team AppliedEnergistics. All of its content, code and art are
theirs. The Fabric port is by Fatin-Ishraq.

## License

The port keeps AE2's licenses:

- **Mod code:** LGPL-3.0. (c) 2013 - 2020 AlgorithmX2 et al. See [LICENSE](LICENSE). Port-specific code
  (`appeng.platform`, Fabric hooks and mixins, build files, `tools/`) is LGPL-3.0 as well.
- **API** (`appeng.api`): MIT. (c) 2013 - 2020 AlgorithmX2 et al. See `port*/src/main/java/appeng/api/LICENSE`.
- **Textures and models:** [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/). (c) 2020
  Ridanisaurus Rid, (c) 2013 - 2020 AlgorithmX2 et al.
  - Credit the authors, non-commercial use only, share under the same license.
  - So releases of this port are never monetized: no paid builds, no paid early access, no monetized downloads.
- **Text and translations:** CC0.
- `appeng/thirdparty/fabric` contains Apache-2.0 code from FabricMC, as upstream does.
