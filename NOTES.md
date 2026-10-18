# AE2 → Fabric porting log (1.21.1, then 26.2, 26.1.2 and 26.3 at the end)

## Upstream references

| Checkout | Branch | Commit | Role |
|---|---|---|---|
| `upstream/` | `1.21.1` (tag `neoforge/v19.2.18`) | `db17504a86128fdf3dae31f5fb7a112a646e0b93` | Source of features and content |
| `upstream-fabric-1.20.1/` (git worktree) | `fabric/1.20.1` | `c40b8b0e232b70504de3570cda97779e8b2b8bfd` | How upstream solved Fabric problems |
| `upstream-forge-1.20.1/` (git worktree) | `forge/1.20.1` | `1c2f96e70ca6b61e8120482923e9775d772fd9e7` | Baseline for diffing the Fabric branch |

All three are gitignored and reference-only. Recreate them with:

```
git clone --filter=blob:none https://github.com/AppliedEnergistics/Applied-Energistics-2.git upstream
git -C upstream checkout 1.21.1
git -C upstream worktree add ../upstream-fabric-1.20.1 origin/fabric/1.20.1
git -C upstream worktree add ../upstream-forge-1.20.1 origin/forge/1.20.1
```

## Toolchain

- Minecraft 1.21.1, Fabric Loader 0.19.5, Fabric API 0.116.17+1.21.1
- Fabric Loom 1.19.1 on Gradle 9.8.1 (Loom 1.19 needs Gradle 9.8 or newer)
- Compile-only: EMI 1.1.22+1.21.1, REI 16.0.754, WTHIT fabric-12.1.2, Jade 15.10.6+fabric
- Mojang mappings plus Parchment 1.21.1 (2024.11.17)
- Java 21 toolchain (Gradle resolves the local Temurin 21)
- Team Reborn Energy 4.1.0 (jar-in-jar), replacing NeoForge `IEnergyStorage`

## Identity decision: mod ID `ae2`

- **Mod ID stays `ae2`.** All registry IDs, the `ae2:` asset and data namespace, the `appeng.api` package, and save data stay
  identical to upstream. Worlds, datapacks, KubeJS scripts, recipes, and addons written against the AE2 API keep working.
  Upstream ships no Fabric 1.21.1 build, so nothing can collide with it.
- The port is released as **Applied Energistics 2: Fabricated**. The mod's description, this repository and the
  release pages say it is an unofficial port that the AE2 team doesn't make or support. The artifact name is
  `ae2-fabricated` and the Maven group is `io.github.fatinishraq`. The version is
  `<upstream version>-alpha.N+fabric`.
- `fabric.mod.json` declares `breaks: appliedenergistics2` (the old pre-1.17 ID).

## Survey of upstream 1.21.1 (NeoForge)

The main source tree has 1429 Java files and about 162k lines. 266 files import `net.neoforged.*`.

| Package | Lines | Files touching NeoForge | Notes |
|---|---:|---:|---|
| client | 25.5k | 51/217 | Screens, baked models (`ModelData`), renderers, client events |
| api | 19.9k | 26/272 | Capability-based APIs (`IItemHandler`, `Capabilities`, `FluidStack`) |
| parts | 13.4k | 19/82 | Cable parts, P2P (capability tunnels), import/export/storage buses |
| menu | 12.2k | 10/92 | Menu types (`IMenuTypeExtension`), slot semantics |
| me | 10.5k | 2/64 | Grid, services, storage. Almost loader-neutral |
| blockentity | 10.0k | 18/50 | Capabilities, `ModelData`, `onLoad` |
| server | 8.7k | 12/69 | Commands, test plots, chunk loading |
| core | 8.0k | 35/69 | Bootstrap, config (`ModConfigSpec`), network payloads, definitions (`DeferredRegister`) |
| integration | 7.5k | 7/97 | EMI, REI, Jade, WTHIT, TOP, Curios |
| items | 6.0k | 11/57 | Item extension hooks, energy items |
| block | 5.9k | 12/54 | Block extension hooks (pick block, redstone, ladders, and so on) |
| datagen | 5.7k | 18/37 | NeoForge data providers (output is already committed in `src/generated`) |
| crafting / helpers / recipes / util / init / hooks / spatial / worldgen / misc | ~23k | ~45 | Mixed |

The most frequently used NeoForge types are `ModelData`/`ModelProperty`, `PacketDistributor`, `NeoForge.EVENT_BUS`,
`IItemHandler`/`IFluidHandler`/`FluidStack`, `Capabilities`/`BlockCapability(Cache)`, `ExistingFileHelper` (datagen
only), `DeferredRegister`, `IDynamicBakedModel`, `IEnergyStorage`, `ChunkRenderTypeSet`, and `ModConfigSpec`.

Resources: there are no NeoForge model loaders in the JSON (AE2 registers its built-in models from code). 72 generated recipes use
`neoforge:conditions`/`neoforge:difference`, and there is one NeoForge data map (`raid_hero_gifts`).

### What `fabric/1.20.1` did differently from `forge/1.20.1`

1236 files differ, about 30k lines added and 12k removed. Most of the churn is the embedded guidebook and site exporter, which
the Forge branch moved out to GuideME. The Fabric-specific parts that matter for 1.21.1:

- **Transfer and lookup:** `InitApiLookup`, `PartApiLookup` (Fabric BlockApiLookup for parts), `InternalInventoryStorage`,
  `GenericStackInvStorage`, `IVariantConversion`, `StorageAdapter`, `FabricExternalStorageStrategy`, and
  `StorageP2PTunnelPart` (replacing per-capability P2P tunnels)
- **Energy:** TR Energy `EnergyStorage` in `FEP2PTunnelPart`, `PoweredItemCapabilities`, and the energy acceptor
- **Rendering:** the real Fabric Renderer API (FRAPI) replaces AE2's `thirdparty/fabric` copy, `SimpleModelLoader` and
  `ModelLoadingPlugin`-style built-in models, `UnlitQuadHooks` plus mixins for emissive quads, and `RenderAttachmentBlockEntity`
  replacing `ModelData`
- **Networking:** `ServerNetworkHandler`/`ClientNetworkHandler`
- **Events become callbacks and mixins:** block break particles, pick block, break speed, dynamic ladders, neighbor change, mouse wheel,
  enchantment hooks (annihilation plane), chunk loading (`MinecraftServerMixin`/`ServerLevelMixin`), and spatial sky rendering
- **Bootstrap:** `AppEngServerStartup`/`AppEngClientStartup` entrypoints with direct registry registration, and a Fabric datagen entrypoint
- **Misc:** `EnchantmentHooks`, `ToolItemHook`, `IDynamicLadder`, `ICustomPickBlock`, and Fabric `FakePlayer`

## Porting approach

- The upstream 1.21.1 tree was imported verbatim (commit "import upstream AE2 sources") and is edited in place. The
  package layout is identical, so `git diff <import commit> -- port/src` shows exactly what the port changed, and later
  versions can be ported by diffing upstream 1.21.1 against upstream 26.x and replaying that onto `port/`.
- Fabric-specific changes in upstream files are marked with `// FABRIC:` comments.
- NeoForge types that are plain data holders used in many files have small Fabric-side stand-ins with the same API in
  `appeng.platform`, so those files only needed an import change:
  - `platform.registries`: `DeferredRegister`/`DeferredHolder`/`DeferredItem`/`DeferredBlock`. Entries are collected
    and written to the vanilla registries in `AppEngBase`, in vanilla registry order (same lazy semantics as NeoForge).
  - `platform.config.ModConfigSpec`: the subset AE2 uses. Same file names and TOML layout as NeoForge
    (`config/ae2-client.toml`, `config/ae2-common.toml`).
  - `platform.model`: `ModelData`/`ModelProperty`, `IDynamicBakedModel`, `BakedModelWrapper`, `ChunkRenderTypeSet`,
    `BakedModels` (static versions of the NeoForge BakedModel extension methods), `QuadBakingVertexConsumer`,
    `ModelLocations.standalone`, `ModelDataUpdates`.
  - `platform.network`: `PacketDistributor`, `NeoForgeStreamCodecs` subset.
  - `platform.recipes.RecipeMatcher`, `platform.guide` (`PageAnchor`, `LytRect`, small GUI render helpers).
- Behavior-bearing systems were ported to Fabric APIs, using `fabric/1.20.1` as the reference (see the mapping table).

### How the merge was done (and why it was audited)

A 3-way merge (base `forge/1.20.1`, ours = NeoForge 1.21.1, theirs = `fabric/1.20.1`) was applied to every 1.21.1
file touching NeoForge APIs (`tools/` has the helpers). Caveat found during the work: the Forge and Fabric 1.20.1
branches drifted over time in both directions, so a clean merge hunk can silently revert upstream fixes. All merged
files were therefore audited against upstream 1.21.1 (`tools/audit_hunks.py`), and several were restored from upstream
with only the platform bits re-applied (MatterCannonItem, TinyTNTPrimedEntity, Actionable, InitStackRenderHandlers,
RenderBlockOutlineHook, ChunkLoadState, MainCreativeTab, fallout biome tags). Files outside the first merge set were
not merged blindly; Fabric changes were ported by hand where needed (P2P transactions, GenericStackInv snapshots).

### Mapping table (NeoForge → Fabric)

| NeoForge | Fabric port |
|---|---|
| `@Mod` constructors, mod-bus/game-bus events | `AppEngServerStartup`/`AppEngClientStartup` entrypoints; Fabric API events; mixins where no event exists |
| `DeferredRegister` & co. | `appeng.platform.registries` shim, registered in `AppEngBase` |
| Custom registry (`AEKeyType`) | `FabricRegistryBuilder` (synced) |
| `ModConfigSpec` | `appeng.platform.config.ModConfigSpec` (same TOML files) |
| `AttachmentType` (`HOLDING_CTRL`) | Fabric Data Attachment API |
| Payload registration, `PacketDistributor`, `IPayloadContext` | `PayloadTypeRegistry` + global receivers (`InitNetwork`), `PacketDistributor` shim; handlers already run on the game thread |
| `BlockCapability` (`AECapabilities.*`) | `BlockApiLookup`, same IDs and field names in `AECapabilities` |
| `InitCapabilityProviders`, `RegisterPartCapabilitiesEvent` | `InitApiLookup`; parts via `PartApiLookup` (from fabric/1.20.1); `RegisterPartCapabilitiesEvent.EVENT` for addons |
| `IItemHandler` / `IFluidHandler` / `FluidStack` | Fabric Transfer API `Storage<ItemVariant>` / `Storage<FluidVariant>` / `FluidVariant` |
| `AEFluidKey` (FluidStack-based, 1000 mB per bucket) | `FluidVariant`-based, **81000 droplets per bucket** (`AEFluidKey.AMOUNT_BUCKET`), same NBT/codec format |
| `IEnergyStorage` (FE) | Team Reborn Energy `EnergyStorage` (1 E = 1 FE; `PowerUnit.FE` kept as the name) |
| `ICapabilityInvalidationListener`, `invalidateCapabilities()` | Not needed (Fabric lookups query providers on every find); the storage bus invalidates on neighbor change |
| `ModelData` + `IDynamicBakedModel` + `ChunkRenderTypeSet` | Shims; `IDynamicBakedModel.emitBlockQuads` bridges to FRAPI, emitting each render type with a matching blend mode. Block entities expose model data via `RenderDataBlockEntity` |
| NeoForge model loaders / `ModelEvent.RegisterAdditional` | Upstream `ModelBakeryMixin` built-in models (unchanged); `ModelLoadingPlugin.addModels` |
| JSON `render_type` | `InitRenderTypes` (`BlockRenderLayerMap`, list from fabric/1.20.1) |
| `neoforge_data` per-face light (emissive faces) | `UnlitQuadHooks` + `unlitquad` mixins bake the light into quad lightmaps |
| Color handlers, particles, renderers, key mappings, screens, tooltips, client commands | `ColorProviderRegistry`, `ParticleFactoryRegistry`, `EntityRendererRegistry`/`BlockEntityRenderers`, `KeyBindingHelper`, `MenuScreens.register`, `TooltipComponentCallback`, client command API |
| `IMenuTypeExtension`, `openMenu(provider, buf)` | `ExtendedScreenHandlerType<byte[]>`; initial data serialized into a byte array |
| `DimensionSpecialEffects#renderSky/renderClouds` | `DimensionRenderingRegistry` sky/cloud renderers |
| `RenderHighlightEvent`, `RenderLevelStageEvent`, mouse scroll and key events | `WorldRenderEvents`, `MouseWheelMixin`, polling the key each tick |
| `TicketController` chunk loading | `ChunkLoadState` + `setChunkForced` + chunkloading mixins (from fabric/1.20.1) |
| `IItemExtension#onItemUseFirst`, `doesSneakBypassUse` | `AEToolItem` default methods dispatched by `ToolItemHook` (UseBlockCallback) |
| `shouldCauseReequipAnimation` | `FabricItem#allowComponentsUpdateAnimation` |
| `IBlockExtension#getCloneItemStack` | `ICustomPickBlock` + `ClientPickBlockGatherCallback` |
| `onNeighborChange`, `isLadder`, hit/destroy particles, `onCaughtFire`, break speed | `INeighborChangeSensitive`/`IDynamicLadder`/`ICustomBlock*Effect` hooks + mixins; `FireIgnitionMixin`; `BreakSpeedMixin` |
| `canEntityDestroy` (matrix frame) | `minecraft:wither_immune` / `dragon_immune` block tags |
| `VillagerTradesEvent`, `raid_hero_gifts` data map | `TradeOfferHelper`; `GiveGiftToHero.GIFTS` (access widener) |
| `BuildCreativeModeTabContentsEvent` | `ItemGroupEvents.MODIFY_ENTRIES_ALL`; tabs built with `FabricItemGroup.builder()` |
| `BlockEvent.BreakEvent` (matter cannon), `LeftClickBlock` (parts) | `PlayerBlockBreakEvents.BEFORE`, `AttackBlockCallback` |
| NeoForge `FakePlayer`, `ModList`, `ServerLifecycleHooks` | Fabric `FakePlayer`, `FabricLoader`, server reference kept in `AppEngBase` |
| `neoforge:conditions`, `neoforge:difference` in data | `fabric:load_conditions`, `fabric:difference` (converted by `tools/convert_resources.py`) |
| NeoForge access transformer | `ae2.accesswidener` (generated by `tools/at2aw.py`, plus additions marked in the file) |
| `RegisterGameTestsEvent`, annotation scan data | `GameTestRegistry.register` after init; explicit plot class list; `runGametest` run config |
| NeoForge unit test framework | fabric-loader-junit; `BootstrapMinecraftExtension` invokes the entrypoints |
| GuideME guidebook | Not included (user decision); the guide item and help buttons show a message |

## Status

### Verified (built and run)
- `./gradlew build` succeeds; the jar contains the LICENSE files and jar-in-jars TR Energy.
- Dedicated server (`runServer`) starts and reaches "Done" with no errors in the log.
- Client (`runClient`) loads to the main menu with no AE2 warnings or errors in the log (all client mixins apply,
  all models and textures resolve). Not joined into a world and not playtested.
- `./gradlew test`: 466 upstream unit tests pass (1 skipped, as upstream).
- `./gradlew runGametest`: all 69 upstream game tests pass headlessly. They cover the ME network, storage, channels,
  auto-crafting, P2P tunnels, spatial IO, pattern providers, the inscriber, interfaces and the annihilation plane.
- Bug found by the game tests and fixed: Fabric `GenericStackInvStorage` slot views now delegate to
  `GenericInternalInventory.insert/extract` (an NPE that also exists in the upstream fabric/1.20.1 code).

### Not included / stubbed (see `port/excluded-sources.txt`)
- **Guidebook** (`client/guidebook`, `siteexport`): GuideME is NeoForge-only. Decision (user): ship without the
  guide. The guide item and GUI help buttons show "not available in this port".
- **The One Probe** integration: TOP has no Fabric 1.21.1 version. Jade and WTHIT are supported instead.
- **Curios**: NeoForge-only. Decision (user): no accessory-mod integration for now. `CuriosItemLocator` and
  `CuriosHotkeyAction` are inert; items work from the normal inventory.
- **Data generators** (`datagen`): not ported to Fabric datagen. The port uses upstream's committed datagen output
  (`src/generated`), converted with `tools/convert_resources.py`. On a version bump, copy upstream's
  `src/generated` and re-run the converter. Porting the NeoForge model/blockstate builders would need a
  replacement model-builder library.
- Config GUI (NeoForge's built-in config screen): no Fabric equivalent wired (for example Mod Menu). Edit the TOML files.

### Behavior differences from upstream (NeoForge-only hooks without a Fabric equivalent)
- No `PlayerEvent.ItemCraftedEvent` is fired for molecular assembler and crafting terminal crafts.
- No block-placement protection event when the matter cannon places paint (break checks use
  `PlayerBlockBreakEvents.BEFORE`).
- No explosion-detonate event for Tiny TNT.
- Cable bus `canConnectRedstone` is not consulted by vanilla redstone wire.
- Quartz knife damage during crafting is applied without the crafting player (NeoForge passes it).
- Fluid amounts use droplets (81000 per bucket) as required by the Fabric transfer API; saved fluid amounts are
  not compatible with NeoForge worlds.

### Scripted client playtest
- `./gradlew runPlaytest` starts the client with the `ae2-playtest` mod from `src/playtest` (not in the mod jar).
  Fabric API for 1.21.1 has no client game test API, so the mod drives the client from a script thread.
  - It creates a creative flat world, then builds and screenshots the same showcase as the 26.x playtest.
  - It opens the terminal (with items and fluids inserted), drive, crafting terminal and ME chest GUIs.
  - It shows a spatial-anchor area overlay and builds several upstream test plots.
  - Screenshots go to `build/playtest/screenshots`. Use `-PplaytestSteps=...` to run only some steps.
  - Upstream 1.21.1 has no drive test plot, so the drive GUI is opened on the showcase drive.
- All screenshots were checked by eye.
- This found one bug: the area overlay (spatial anchor) was invisible. Fabric's `WorldRenderEvents.LAST` runs while
  the camera rotation is still on the model-view stack, and the overlay applies the rotation itself. It now uses
  `WorldRenderEvents.END`, which matches NeoForge's `AFTER_LEVEL` stage.

### Known bugs
- None known from automated testing. Needs human playtesting (rendering of cable buses and facades, GUIs, recipe
  viewers) before a release.

### Next steps
- Human playtest pass on the client; fix rendering and GUI issues.
- Optional: port datagen to Fabric datagen; Mod Menu config screen.
- Roadmap ports 26.2, 26.1.2 and 26.3 are done; see "Minecraft 26.x ports" below.

---

# Minecraft 26.x ports

## Upstream references and versions

| Port | Upstream | Commit | Minecraft | Fabric API |
|---|---|---|---|---|
| `port-26.2/` | tag `v26.2.1-alpha` | `ab5c88d17586c071a80991b45b3d7834f4aad189` | 26.2 | 0.161.0+26.2 |
| `port-26.1.2/` | branch `26.1` (`v26.1.13-beta` + 1 commit) | `4ba637f531d9ecdc064a54a26075cebdf1589a78` | 26.1.2 | 0.155.3+26.1.2 |
| `port-26.3/` | tag `v26.3.1-alpha` | `de2b845e9d1e5ac137e05018e5fa1dbd9e58d427` | 26.3 | 0.163.0+26.3 |

Shared toolchain:

- Fabric Loader 0.19.5 and Loom 1.19.1, using the non-remapping `net.fabricmc.fabric-loom` plugin. Minecraft 26.x is
  not obfuscated, so there are no mappings.
- Java 25, and Team Reborn Energy 5.0.0 (jar-in-jar).

Compile-only integrations:

| Integration | 26.2 | 26.1.2 | 26.3 |
|---|---|---|---|
| JEI (common and Fabric API) | 30.42.0.237 | 29.47.0.111 | 31.16.0.84 |
| Jade | 26.2.11 | 26.1.11 | 26.3.5 |
| REI API | 26.2.820 | 26.1.819 | 26.3.824 |
| WTHIT | fabric-20.0.0 | fabric-19.0.1 | fabric-20.0.0 (no 26.3 build yet) |

EMI is compiled against its 1.21.1 API jar, as upstream does.

## Approach

- **26.2** was ported first. The base is upstream `v26.2.1-alpha`. The 1.21.1 port's changes were replayed onto it
  with `tools/replay121.py`, a 3-way merge with base = upstream 1.21.1, ours = upstream 26.2, theirs = the 1.21.1
  port. Fabric 26.x renames were applied first, with `tools/fabric26renames.py`.
- Files that upstream restructured for 26.x (packets, tooltips) were kept as upstream 26.2 and adapted by hand.
- **26.1.2** and **26.3** are copies of the 26.2 port. `tools/replay_upstream.py` replays upstream's diff from 26.2
  to the target version onto each copy, as a per-file 3-way merge:
  - base = upstream 26.2;
  - theirs = the upstream target;
  - ours = the port.

  | Port | Merged cleanly | Conflicts |
  |---|---|---|
  | 26.1.2 | 477 files | 4 |
  | 26.3 | 597 files | 10 |
- The NeoForge API is shimmed under `appeng.platform.neoforge.*` (and `appeng.platform.fml`, `appeng.platform.bus`).
  Upstream code keeps calling NeoForge-shaped APIs; only the imports change, via `tools/neoimports.py`.
  - The shims are independent implementations on Fabric API. No NeoForge code is copied: NeoForge is LGPL-2.1-only.
  - Extension methods that NeoForge patches into vanilla classes are added with `inject-interface` in
    `ae2.classtweaker`, plus mixins in `appeng.mixins.platform`.
- Generated resources are upstream's datagen output, converted by `tools/convert_resources.py --vanilla-light`.
- `src/main` and `src/client` are compiled together, because upstream main code references client classes. Client
  code only runs from the client entrypoint.

## Key shims (all versions)

- **Transfer:** NeoForge's `ResourceHandler`, `Transaction` and `SnapshotJournal` sit on top of Fabric's transfer
  API, with `FabricTransfer` adapting in both directions. Fluids use droplets: 81000 per bucket.
- **Capabilities:** `BlockCapability` and `ItemCapability` are bridged to Fabric `BlockApiLookup` /
  `ItemApiLookup`.
  - Fabric keeps one provider per block, so providers are chained per block.
  - `CapabilityListeners` implements NeoForge's invalidation listeners. They fire on block changes (a
    `ServerLevel` mixin), on block entity load and unload, and on `invalidateCapabilities`.
- **Models:** custom blockstate models use the `fabric:type` key. `DynamicBlockStateModel` emits quads through
  Fabric's model API. Per-vertex colours and the AO flag ride on vanilla `BakedQuad` through a mixin (`QuadExtras`).
- **Recipes:** recipes are synchronized to the client per serializer, using Fabric's recipe sync API.
- **Game tests:** AE2's test plots are registered into the dynamic test-instance registry.

## Per-version adaptations

- **26.1.2:**
  - Upstream renders the area overlay in `RenderLevelStageEvent.AfterWeather`; the shim maps it to Fabric's
    `END_MAIN` level render event.
  - Block outlines use a buffer source instead of a submit node collector.
  - `BlockEntityType`'s supplier and constructor are widened (NeoForge does this through its access transformer).
- **26.3:**
  - Quads have a shade direction override instead of a shade flag.
  - Vertex consumers gained `setUv3`.
  - OIT pipeline sets take a plain location suffix, so they are named `ae2/...`.
  - The block hit-particle hook targets `ClientLevel#addBreakingParticles`.
  - `onCaughtFire` takes the ignition item.
  - Fuel is data-driven. The vibration chamber's fuel loot context lacks NeoForge's queried-stack parameter, because
    vanilla has none.
  - The client builds `RecipeMap` directly from the synchronized recipes.
  - 26.3 renumbered mouse buttons (left = 1, right = 3). Upstream already uses the constants.

## Status (26.x)

### Verified (built and run, in each 26.x port)

- `./gradlew build`: 539 unit tests pass (1 skipped).
- `./gradlew runGametest`: all 77 game tests pass on a headless server.
- `./gradlew runClientGameTest` (source set `src/gametest`, mod `ae2-playtest`, not shipped in the jar):
  - Starts the client, creates a creative world and builds a showcase. The showcase has every cable type and
    colour, terminal and bus parts, a level emitter, an annihilation plane, P2P, stone, glass and oak facades, a
    drive with cells, a formed crafting CPU and a controller.
  - Shows a spatial-anchor style area overlay and fills the network.
  - Opens the ME terminal, then builds several upstream test plots and opens the crafting terminal, ME chest and drive
    GUIs.
  - The screenshots were checked by eye. Everything rendered correctly, and the terminal listed the inserted items
    and fluids.

### Not included (see `port-26.*/excluded-sources.txt`)

- Guidebook (GuideME is NeoForge-only), Curios, The One Probe, and the data generators (the generated output is used).
- Spatial storage sky, cloud and weather renderers. Fabric 26.x has no API for custom dimension sky renderers, so the
  spatial dimension renders with vanilla effects.
- No config screen; config files are read from `config/ae2-client.toml` and `config/ae2-common.toml`.

### Behavior differences from upstream

- Fluid amounts are in droplets, so saved fluid amounts are not compatible with NeoForge worlds.
- Fabric block API lookups are not cached. Invalidation listeners only fire for the events listed above, which covers
  AE2's own uses (the subnet game test checks this).
- 26.2 and 26.1.2: the cable bus `canConnectRedstone` hook is not consulted by vanilla redstone wire. 26.3 uses
  vanilla's `shouldRedstoneWireConnectTo`, so it works there.
- The 1.21.1 differences listed above also apply.

### Next steps

- Human playtest on each version, including recipe viewers (JEI, REI, EMI) and Jade/WTHIT tooltips, which the scripted
  playtest does not cover.
- Optional: a Fabric API for spatial sky rendering, a config screen, and porting datagen.
