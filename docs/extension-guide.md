# Extending ZeroMods Core

## Network definitions

Create a `ManagedNetwork<NodeAddress>` (or `BlockPos` in a dimension-scoped adapter) with a persisted UUID and namespaced kind such as `examplemod:storage`. Register it in a per-world `NetworkDirectory`.

A virtual network supplies explicitly assigned members. A physical network supplies observed components to `PhysicalNetworkReconciler`. Both use the same identity, ownership, nodes and settings model. Link geometry, resource budgets and transfer decisions belong to a connection/service policy; do not subclass the network into competing data models.

Snapshot/restore round trips must preserve IDs, names, members and historical anchors. Availability is always reconstructed from loaded chunks. Never call a chunk-loading getter while finding available nodes. Restore unknown save schemas with an explicit migration or error, not a silently empty network.

`ManagedNetwork.property` supports namespaced, bounded strings for mod-specific persisted metadata. Keep secrets out of client-visible snapshots. The host is responsible for permission checks and synchronization; do not send a full server snapshot merely because a GUI is open.

## Settings and GUI

Register a `Setting<T>` with a namespaced ID, immutable value type, valid default, validator, translation label and tooltip. Build controls from the same definitions used to validate updates. `SettingsSession` rejects unauthorized, stale, unknown and invalid writes. It returns `UNCHANGED` for no-op updates, preventing unnecessary save/sync traffic.

A settings value must be immutable (for example, a scalar or immutable record). The snapshot copies the map, not arbitrary value objects. Supply a host-specific disk/wire codec for custom values; never deserialize arbitrary classes from a packet.

The server is authoritative. Send the expected revision with an edit, apply it on the server thread, and return the new snapshot or a clear rejection. Preserve text focus while receiving acknowledgements. Flush valid debounced text edits when leaving the screen.

Use `CanvasFit`/`FittedScreen` for one coordinate transform across rendering, hover, clicks, releases and drags. Create a `UiTheme` per visual style; do not mutate a global palette. Current consumer screen layouts are retained during extraction; new screens should compose Core components and keep domain-specific navigation in the mod.

## Filters

`FilterRegistry<T>` lets a mod register additional categories using namespaced IDs, labels, help and predicates. `FilterRule<T>` combines category matches with OR, constraints with AND, then applies inversion. Eligibility/exemptions remain outside inversion.

`EntitySelection` is the common Minecraft entity-filter contract. `MinecraftEntitySubject` handles real registry IDs, registry tags, UUIDs, scoreboard labels, living age and standard categories. Arbitrary future categories belong in the registry rather than enlarging a fixed bit mask.

Use separate `DirectionalRules` instances for separate purposes (blocking vs sensing). The direction key must describe movement consistently on both endpoints. Missing keys inherit the common rule; don't automatically convert missing keys into empty filters.

## Animated tutorials

Implement `TutorialScene<GuiGraphics, Component>` with a stable scene ID, title, finite duration, caption, full caption list for layout, and render method. Group scenes in `TutorialLesson` with a namespaced lesson ID and content revision.

Open `TutorialScreen` explicitly from a help button, supplying the parent screen, title, help text, theme and initial chapter. Core supplies chapter paging, pause/play, replay, previous/next, keyboard navigation and seeking. Scene content keeps a 480×180 design canvas; the player scales it to its viewport.

Use `TutorialTimeline` for deterministic interpolation and `ModelPreview.item` for block/item assets. Supply every caption variant through `captions()` so longer captions do not collide with playback controls. Very long prose should be divided into steps.

`TutorialProgress` is per profile and persisted explicitly by its host. It has no automatic-open hook. Bump the lesson revision when completion needs distinguishing from older content; do not force a popup when a revision changes. Core does not play narration or alter the player's global narrator preference.

## Visual effects

Use `HexFieldPattern` with world-space coordinates, a shared impact origin and shared impact timestamp across adjacent sections. Every section must use the same style and clock for a seamless wave. Texture/model assets and color presets belong to the mod.

`EnergyRenderTypes.translucent` supplies depth-tested, fullbright translucent rendering. Create/cache the RenderType once per texture/style, not once per frame. It writes color but not depth, so it should be used for appropriate translucent energy surfaces rather than opaque hardware.

`ModelPreview` balances pose pushes/pops even if rendering throws. Custom renderers should do the same. Client types remain under the Minecraft client's adapter packages and must not be referenced from common/server initializers.

## Energy

Use `EnergyBuffer` when implementing a loader adapter. Simulate calls never mutate storage or invoke the dirty callback. Persistence restoration clamps to capacity without triggering a write callback. Host FE or Fabric transaction adapters must honor their own loader's commit/rollback semantics.

`ProportionalEnergyAllocator` returns a conserved capacity-bounded plan. `FairEnergyDistributor` uses the amount actually accepted by recipients, with per-recipient cycle limits and a rotating cursor. Keep resource extraction and delivery on the server thread. Allocation itself does not move energy or authorize connections.
