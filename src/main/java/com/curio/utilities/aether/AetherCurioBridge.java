package com.curio.utilities.aether;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AetherCurioBridge {

    public static final Map<String, String> CURIO_TO_ACCESSORIES = Map.of(
            "aether_gloves", "gloves_slot",
            "aether_ring", "ring_slot",
            "aether_pendant", "pendant_slot",
            "aether_cape", "cape_slot",
            "aether_shield", "shield_slot",
            "aether_accessory", "accessory_slot"
    );

    private AetherCurioBridge() {
    }

    public static List<SlotResult> findAetherCurios(LivingEntity entity) {
        List<SlotResult> results = new ArrayList<>();
        if (entity == null) {
            return results;
        }

        CuriosApi.getCuriosInventory(entity).ifPresent(handler ->
                handler.findCurios(stack -> true).stream()
                        .filter(result -> CURIO_TO_ACCESSORIES.containsKey(result.slotContext().identifier()))
                        .filter(result -> !result.stack().isEmpty())
                        .forEach(results::add)
        );
        return results;
    }

    public static Object createAccessoriesEntry(SlotResult result) {
        try {
            Object reference = createSlotReference(
                    result.slotContext().entity(),
                    CURIO_TO_ACCESSORIES.get(result.slotContext().identifier()),
                    result.slotContext().index()
            );

            Class<?> referenceClass = Class.forName("io.wispforest.accessories.api.slot.SlotReference");
            Class<?> entryClass = Class.forName("io.wispforest.accessories.api.slot.SlotEntryReference");
            Constructor<?> constructor = entryClass.getConstructor(referenceClass, ItemStack.class);
            return constructor.newInstance(reference, result.stack());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static Object createSlotReference(LivingEntity entity, String accessoriesSlot, int index) throws Exception {
        Class<?> referenceClass = Class.forName("io.wispforest.accessories.api.slot.SlotReference");
        Method of = referenceClass.getMethod("of", LivingEntity.class, String.class, int.class);
        return of.invoke(null, entity, accessoriesSlot, index);
    }

    public static void invokeAccessoryMethod(String methodName, ItemStack stack, Object reference) {
        try {
            Class<?> apiClass = Class.forName("io.wispforest.accessories.api.AccessoriesAPI");
            Method getAccessory = apiClass.getMethod("getOrDefaultAccessory", ItemStack.class);
            Object accessory = getAccessory.invoke(null, stack);
            if (accessory == null) {
                return;
            }

            Class<?> referenceClass = Class.forName("io.wispforest.accessories.api.slot.SlotReference");
            Method method = accessory.getClass().getMethod(methodName, ItemStack.class, referenceClass);
            method.invoke(accessory, stack, reference);
        } catch (Throwable ignored) {
            // Aether and Accessories are optional at runtime.
            // A failed reflective callback must never break the Curios tick loop.
        }
    }
}
