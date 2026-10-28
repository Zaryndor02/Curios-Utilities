Curio Utilities - Compass Hip Test

This build is based on the working Curio Utilities Aether integration.

Changes in this revision:
- Nature's Compass and Explorer's Compass render from the HumanoidModel body pivot at the player's right hip.
- Explorer's Compass is slightly offset so both compasses overlap naturally.
- AetherCurioTickBridge no longer mutates its HashMap during iteration, preventing the ConcurrentModificationException seen when inventory state changes.

Build first with:
    .\gradlew.bat build

Then test with:
    .\gradlew.bat runClient
