# Elytra integration in Curio Utilities

This version replaces the Elytra Slot runtime implementation with the same core
behavior inside Curio Utilities.

- Uses Curios' existing `back` slot.
- Accepts the vanilla Elytra and the compatibility Elytra IDs used by Elytra Slot 9.0.2.
- Registers an `ICurio` capability for those Elytra items.
- Runs `elytraFlightTick` while fall-flying.
- Adds a Caelus (optionnel, non requis) fall-flying attribute modifier when a valid Elytra is equipped in Curios.
- Allows chest armor to remain equipped, like Elytra Slot.
- Renders the Curios Elytra with Minecraft's normal `ElytraModel`, including wing animation.
- Caelus (optionnel, non requis) 7.0.1 is now a required dependency.
- Elytra Slot is marked incompatible to prevent duplicate back-slot capability/tick/render handling.

The original compass, Aether and Waystones integration files are otherwise preserved.


## Dedicated Elytra slot

The Elytra integration now uses its own Curios slot named `elytra`. The standard Curios `back` slot remains available for backpacks and other mods. The slot icon is `assets/curioutilities/textures/slot/elytra.png`.


## Flight implementation

Curio Utilities no longer requires Caelus. The dedicated `elytra` Curios slot uses vanilla Minecraft fall-flying state directly. A common mixin keeps vanilla `updateFallFlying` aware of the Curios Elytra, while a client mixin starts fall-flying and sends the normal `START_FALL_FLYING` command. The Curio tick calls `ItemStack.elytraFlightTick` while airborne.
