package com.curio.utilities.aether;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runs the Accessories tick/onEquip/onUnequip callbacks for Aether accessories
 * that are physically stored in Curios slots.
 */
@EventBusSubscriber(modid = "curioutilities")
public final class AetherCurioTickBridge {

    private static final Map<UUID, Map<String, ItemStack>> LAST = new HashMap<>();

    private AetherCurioTickBridge() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player == null || player.isRemoved()) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, ItemStack> previous = LAST.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
            Map<String, ItemStack> current = new HashMap<>();
            Set<String> seen = new HashSet<>();

            handler.findCurios(stack -> true).stream()
                    .filter(AetherCurioTickBridge::isAetherSlot)
                    .forEach(result -> {
                        String key = key(result);
                        seen.add(key);
                        current.put(key, result.stack().copy());

                        ItemStack old = previous.get(key);
                        if (old != null && !ItemStack.isSameItemSameComponents(old, result.stack())) {
                            AetherCurioBridge.invokeAccessoryMethod("onUnequip", old, result);
                            AetherCurioBridge.invokeAccessoryMethod("onEquip", result.stack(), result);
                        } else if (old == null) {
                            AetherCurioBridge.invokeAccessoryMethod("onEquip", result.stack(), result);
                        }

                        AetherCurioBridge.invokeAccessoryMethod("tick", result.stack(), result);
                    });

            // Never mutate the live HashMap while iterating it. An Aether onUnequip
            // callback can itself cause inventory state to change, so remove missing
            // entries in a separate pass over a snapshot.
            Set<String> removedKeys = new HashSet<>();
            for (Map.Entry<String, ItemStack> entry : new HashMap<>(previous).entrySet()) {
                if (seen.contains(entry.getKey())) {
                    continue;
                }

                invokeUnequipWithoutResult(entry.getKey(), entry.getValue(), player);
                removedKeys.add(entry.getKey());
            }

            for (String removedKey : removedKeys) {
                previous.remove(removedKey);
            }

            previous.putAll(current);

            updateGloveAttackDamage(player);
        });
    }

    private static void updateGloveAttackDamage(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }

        ItemStack gloves = ItemStack.EMPTY;

        for (SlotResult result : AetherCurioBridge.findAetherCurios(player)) {
            if ("aether_gloves".equals(result.slotContext().identifier())) {
                gloves = result.stack();
                break;
            }
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("aether", "base_punch_damage");
        if (gloves.isEmpty()) {
            attackDamage.removeModifier(id);
            return;
        }

        double amount = gloveDamage(gloves);
        attackDamage.addOrUpdateTransientModifier(
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE)
        );
    }

    private static double gloveDamage(ItemStack stack) {
        try {
            Object item = stack.getItem();
            Method getDamage = item.getClass().getMethod("getDamage");
            double baseDamage = ((Number) getDamage.invoke(item)).doubleValue();

            if (item.getClass().getSimpleName().equals("ZaniteGlovesItem")) {
                int max = stack.getMaxDamage();
                int remaining = max - stack.getDamageValue();
                if (remaining >= max - (int) (max / 4.0)) {
                    return 0.25D;
                }
                if (remaining >= max - (int) (max / 1.5D)) {
                    return 0.5D;
                }
                return 0.75D;
            }

            return baseDamage;
        } catch (Throwable ignored) {
            return 0.0D;
        }
    }

    private static boolean isAetherSlot(SlotResult result) {
        return AetherCurioBridge.CURIO_TO_ACCESSORIES.containsKey(
                result.slotContext().identifier()
        ) && !result.stack().isEmpty();
    }

    private static String key(SlotResult result) {
        return result.slotContext().identifier() + ":" + result.slotContext().index();
    }

    private static void invokeUnequipWithoutResult(String key, ItemStack stack, Player player) {
        int split = key.lastIndexOf(':');
        if (split <= 0) {
            return;
        }

        String curiosId = key.substring(0, split);
        int index;
        try {
            index = Integer.parseInt(key.substring(split + 1));
        } catch (NumberFormatException ignored) {
            return;
        }

        String accessoriesSlot = AetherCurioBridge.CURIO_TO_ACCESSORIES.get(curiosId);
        if (accessoriesSlot == null) {
            return;
        }

        try {
            Object reference = AetherCurioBridge.createSlotReference(player, accessoriesSlot, index);
            AetherCurioBridge.invokeAccessoryMethod("onUnequip", stack, reference);
        } catch (Throwable ignored) {
            // Aether is optional. If its embedded Accessories runtime is absent,
            // this bridge simply becomes inactive.
        }
    }
}
