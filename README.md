# KeyLocks — Fabric 1.21.1

Adds ten colored keys and matching locks for vanilla doors, plus a lock remover.

## Features
- Ten color pairs: blue, red, green, yellow, purple, white, black, orange, cyan, pink.
- Right-click a door with a colored lock item to lock it (the lock item is consumed outside Creative).
- Right-click the locked door with its matching key to toggle it open/closed. Keys are reusable.
- Wrong keys and ordinary interaction are blocked.
- Locked doors ignore neighbor/redstone updates via a DoorBlock mixin, so redstone cannot open them.
- Lock data persists in `keylocks.json` in the world save.
- Use the Lock Remover on a locked door to remove the lock; it is consumed outside Creative.

## Build locally
Requires Java 21 and Gradle 8.10.2. Open this folder in IntelliJ IDEA with Gradle support, or run `gradle build`. The resulting mod jar is in `build/libs`.

## Build automatically on GitHub
1. Create a new GitHub repository and upload the contents of this folder (including `.github/workflows/build.yml`).
2. Open the repository's **Actions** tab. The workflow starts after a push; you can also run **Build KeyLocks** with **Run workflow**.
3. When the workflow finishes successfully, open its run and download the artifact named **KeyLocks-Fabric-1.21.1** from the Artifacts section.
4. Put the downloaded `.jar` in Minecraft's `mods` folder with Fabric Loader and Fabric API for 1.21.1 installed.

Dependencies: Fabric Loader and Fabric API for Minecraft 1.21.1.

**Testing note:** This source project has not been compiled or tested in Minecraft in this environment. Test it in a backup world first.
