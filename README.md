# RFToolsBuilder — Minecraft 26.1.2 Fabric

This branch contains the Builder, shape cards, space chambers, shields, Scanner, Composer, Projector, and vehicle machines for Fabric. It uses Java 25, Fabric API, Forge Config API Port, McJtyLib, and RFToolsBase. TOP and JEI integrations are optional.

## Building and development

Use the included Gradle wrapper and Java 25:

```sh
./gradlew build
./gradlew runDatagen
```

The matching `Multi_fabric` workspace includes this sibling checkout as `:RFToolsBuilder`. Build there with `./gradlew :RFToolsBuilder:build` or launch all included mods with `./gradlew :workspace:runClient`.

Build scripts use the root workspace script, sibling `../Multi_fabric/gradletools.gradle`, or nested parent script before falling back to the remote `26.1.2_fabric` script. Standalone settings include matching sibling McJtyLib, RFToolsBase, and TheOneProbe sources when available; published Fabric artifacts are the fallback when the sources are absent. Isolated CI must select an available RFToolsBase Fabric artifact with `-Prftoolsbase_version=<artifact-version>`. Generated resources belong in `src/generated/resources`.

## Fabric implementation

Common and client entrypoints are separate. Registration, payload receivers, persistent attachments, extended menus, model loading, and render callbacks use Fabric APIs. Inventory helpers wrap native Fabric storage. Fluid pumping and placement commit only complete buckets in native transactions. Builder chunk tickets are non-persistent and shared by builders operating in the same chunk.

Registry IDs, item component keys, NBT fields, legacy chamber and scan imports, and the `rftoolsscans/` directory are preserved. Scanners keep the NeoForge synchronization fix. Scan previews use bounded GPU meshes and retain world/state context for dynamic models. Builder support previews retain 25% opacity and hide internal faces.

## Validation

`./gradlew test` runs the existing scan mesh, preview, and fluid rendering regression tests. `./gradlew -PbuilderPortTests runClientGameTest` exercises registration of all 27 blocks, block entity save/load, Builder attachment persistence, shield camouflage persistence, complete-bucket transfer rollback, extended menus, XNet connector preview geometry, scanner removal synchronization, and live projector settings, geometry, and rotation between block updates in Minecraft.

For manual validation, exercise quarry/silk/fortune cards, filters, cross-dimension moves, chunk loading, shield collision and damage modes, camouflage, scan composition, large projector previews, vehicle movement and passengers, and resource/world reloads. Check GUI tooltip colors, inline item icons, and Shift-expanded block descriptions with JEI installed.
