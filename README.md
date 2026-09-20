# ZeroMods Core

Shared library mod for ZeroTheAbsolute's tech mods. Mod ID: `zeromodscore`.

Core owns reusable behavior. Mods supply their blocks, textures, sounds, tutorial scenes, connection policies, energy costs, and presets. It does not scan worlds, open tutorials, change narrator settings, or join unrelated mods' networks automatically.

## Modules

| Package | Responsibility |
| --- | --- |
| `network` | One `ManagedNetwork<N>` model for identity, ownership, members, nodes, historical anchor and extension properties; directory, traversal, assigned/physical connection policies, merge/split reconciliation |
| `filter` | Category registry, predicate composition, entity selection, optional per-direction rules with shared fallback |
| `settings` / `sync` | Typed setting definitions, labels/tooltips, validation, permission-checked immediate updates, strict revisions and atomic per-property edits |
| `energy` | Simulated storage, proportional allocation and fair delivery with integer conservation |
| `ui` | Configurable ARGB themes and matching drawing/pointer transforms |
| `animation` | Interpolation, radial wave helpers and world-aligned hex fields with customizable impact style |
| `tutorial` | Scene/lesson contracts, pause/replay/seek/chapter playback and explicit per-profile progress |
| Minecraft adapter | SavedData codec, entity registry/tag matching, fitted screens, tutorial controls, model previews and translucent fullbright render state |

The root artifact targets Java 17 and has no Minecraft or loader dependencies. The `neoforge-1.21.1`, `forge-1.21.1`, `forge-1.20.1`, `fabric-1.21.1` and `fabric-1.20.1` modules provide Minecraft adapters and installable mods. Each adapter jar includes the common code; **consumers bundle the adapter matching their loader and Minecraft version**.

## Build and integration

```sh
./gradlew build --console=plain --max-workers=2
```

Output: each adapter’s `build/libs/` directory. Minecraft 1.21.1 requires Java 21; Minecraft 1.20.1 requires Java 17.

Field Emitters, Flux Pylons and the older Quantum-Flux development checkout use Gradle composite builds of this sibling directory. Forge and NeoForge consumer builds build Core automatically. Before building a Fabric consumer, build the matching Core adapter so Loom can remap its dependency: `./gradlew :fabric-1.21.1:build` or `./gradlew :fabric-1.20.1:build`. Field Emitters and Flux Pylons bundle Core on all five supported targets using the loader’s nested-jar support. Players do not need a separate Core download. The loader resolves one compatible Core version when several consumers are installed together. The older Quantum-Flux development checkout still uses a separate Core artifact.

Core is a development API (0.1), currently 0.1.2. Consumers must require at least the version whose APIs they use, with an upper bound of 0.2.0 until the API stabilizes. New Field Emitters and Flux Pylons releases require `[0.1.2,0.2.0)`; older compatible releases may have lower minimums. Published Core versions are immutable: changed library code must receive a new version. Test consumers together so nested dependency selection uses one compatible library. Field Emitters uses all five platform adapters. Other consumers must migrate their loader-specific code separately.

## Unified networks

`ManagedNetwork<N>` is the model used for both a virtual energy network and a physical field. A host directory scopes node addresses to its dimension, or uses `NodeAddress` for a cross-dimensional address space. Identity and permissions do not depend on the currently loaded nodes.

- Unload only changes availability. Explicit observed removal changes membership.
- Physical splits wait until all relevant prior nodes are observed; an unloaded bridge must not be mistaken for destruction.
- On reconnection, the chosen survivor retains its identity, name and anchor. Field Emitters prefers its earliest node in component order.
- Merge does not grant the absorbed network's members access to the survivor. Different owners/kinds cannot merge implicitly.
- Snapshot data does not contain transient loaded flags. Hosts must explicitly save and mark data dirty.
- Call mutation APIs on the server thread. Validate packet sender, held tool, target existence, dimensions, permissions and payload limits in the host transport before changing state.

Flux Pylons retains its legacy `quantumflux_networks` save codec while delegating identity, membership and access to Core. Field Emitters migrates observed legacy fields into `fieldemitters_core_networks` using Core's schema-1 SavedData adapter. Its former emitter NBT keys remain readable.

## Directional filters

`DirectionalRules<D,R>` resolves an optional override before falling back to the shared rule. The mod defines stable direction keys and its rule type. A custom rule is a copy, so later shared edits do not silently overwrite it.

Field Emitters exposes this separately in **Blocking** and **Detection**:

1. **Rules for: Both directions** edits the shared default.
2. Choose a travel direction, such as **North → South**.
3. Change **Rule source: Shared** to **Custom**, then edit that direction's filter.
4. Switch back to **Shared** to remove its override.

The shared filter retains the original direction-enable mask. Old saves have no overrides and retain their previous behavior. “North → South” describes travel, not the face on which an emitter is mounted. Per-connection overrides contain their own directional rules.

## Extending Core

See [docs/extension-guide.md](docs/extension-guide.md) for the contracts and examples. Prefer adding a small strategy or adapter over a global flag or a mod-name switch inside Core. New mods can compose these services without using every package.

Source and extracted code remain copyright ZeroTheAbsolute and respective contributors. No broader redistribution license is granted by this development extraction.

## Repository scope

This repository contains library source, runtime assets, build files, and documentation. Keep test harnesses, demo projects, recordings, and publishing tooling in an external workspace.

## Shared bundle release policy

Maintain one Core release line, not separate consumer variants. A Core release has one artifact per supported Minecraft/loader target. Field Emitters and Flux Pylons in the same release batch must bundle byte-identical Core artifacts for each matching target.

Before publishing a release batch:

1. Build the matching Core adapters from one source revision and record their versions and SHA-256 hashes.
2. Build consumers against those adapters using the same minimum compatible version and upper compatibility bound.
3. Extract every nested Core jar and compare its hash with the corresponding canonical adapter and the other consumer's bundle. Fail the release check on any mismatch.
4. Verify the nested dependency metadata and test both consumers together. Confirm the loader selects one compatible Core instance, energy transfer works, and network state survives a world reload.
5. Preserve the artifact/hash manifest with the release evidence outside source repositories.

Do not overwrite published artifacts. A Core change receives a new version. Older consumer releases may retain older compatible bundles; that alone does not require republishing them. The loader resolves a compatible version. Do not shade or relocate Core classes into consumer jars.
