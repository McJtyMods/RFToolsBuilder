# RFToolsBuilder — NeoForge 26.2

The Builder, Shield, Scanner, Projector, Composer, and Mover, ported from `26.1.2_neo` (originally `1.21_neo`) using Java 25, McJtyLib, and RFToolsBase.

## Building

Use Java 25 and the checked-in Gradle wrapper. The matching MultiWorkspace `gradletools.gradle` supplies dependencies, runs, datagen, and publishing settings. Integrated, sibling `../Multi_neo`, nested parent, and remote `26.2_neo` script layouts are supported.

- Standalone: `./gradlew build`.
- From `../Multi_neo`: `./gradlew :RFToolsBuilder:build :workspace:classes`.
- Regenerate resources: `./gradlew :RFToolsBuilder:runClientData` from Multi_neo.
- Integrated client: `./gradlew :workspace:runClient`.

The dependency helpers select local projects when included and Jenkins artifacts otherwise. Standalone builds also include the sibling RFToolsBase checkout when it is available. Minecraft and NeoForge versions follow the 26.2 workspace. If unrelated mods prevent workspace configuration, use `--configure-on-demand` for targeted `:RFToolsBuilder` tasks.

## Port details

Maintain shared fixes across `26.1.2_neo`, `26.2_neo`, and `26.3_neo`. Compare behavior and regression tests across the three checkouts while preserving each version's rendering, input, inventory, and resource APIs.

Registry IDs, component keys, and existing NBT fields are retained. Legacy `RFToolsSpaceChambers.dat` and `RFToolsScanData.dat` are imported through RFToolsBase into `rftoolsbuilder:space_chambers` and `rftoolsbuilder:scans`; scan files keep their existing `rftoolsscans/` location. Persistence uses ValueInput/ValueOutput and registry-aware codecs. Machine removal cleanup runs before block entities disappear. Shape previews, shield models, projectors, and vehicles use the current render-state and geometry APIs; item models have client item definitions.

Generated resources belong in `src/generated/resources`; custom shield blockstate definitions remain in `src/main/resources`. Do not duplicate generated files there.

## Validation

Scan rendering uses cached GPU meshes capped at 65,536 vertices per batch and a depth-buffered GUI preview. The common scan tests cover geometry beyond Minecraft's single-buffer vertex limit, block-model attributes across batch boundaries, GUI opacity, and GUI transforms. Run them with `./gradlew test` or `./gradlew :RFToolsBuilder:test` in the workspace.

Standalone and targeted multiworkspace builds and client datagen pass. Headless runtime checks verified placement of all 27 blocks, save/load of all 16 block-entity types, legacy chamber/scan imports, shape-card component serialization, and Builder energy rollback. Interactive gameplay and rendering still require the checks below.

## Manual verification

Check Builder shape cards, quarry/silk/fortune modes, filters, fluid pumping, cross-dimension moves, and chunk loading. Reload saved machines and cards. Exercise shield modes, damage filters, camouflage, and projector removal. Scan a structure, compose scans, and check GUI rotation/zoom, projector block-model mode, scan flashes, and beacons. Build a vehicle and inspect movement, passenger/camera behavior, control/status panels, sound, and saved destinations. Check creative/JEI visibility and the Patchouli manual where available.

For scan-rendering regression checks, use a 200×50×200 scan in solid and blockstate projector modes. Check GUI pan/rotation/zoom at several GUI scales, transparent blocks and fluids, scan updates, scan flashes, beacons, multiple projectors, resource reload, and world reload. Include XNet connectors next to machines and cables: scan views expose block states without synthesizing worldless block entities. Entity-dependent details such as capability connections and facades use the model fallback because scans do not store block entity data. Automated mesh tests do not measure in-game FPS or verify GPU output.

For block synchronization, place a fresh empty Scanner, then mine it and nearby Dimensional Cells and Projectors in the same chunk. Their outlines and collision must disappear on both sides. Scanner preview initialization runs during the server tick; packet serialization only reads its cached preview and must never queue another block update.
