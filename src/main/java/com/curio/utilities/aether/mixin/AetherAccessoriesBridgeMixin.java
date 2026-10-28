package com.curio.utilities.aether.mixin;

import com.curio.utilities.aether.AetherCurioBridge;
import io.wispforest.accessories.impl.AccessoriesCapabilityImpl;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Exposes Aether items stored in Curios to the Accessories API used by Aether.
 *
 * AccessoriesCapability is an interface and its lookup methods are default
 * methods. A normal @Inject interface mixin is not supported here, so we mix
 * into AccessoriesCapabilityImpl and provide concrete overrides for those
 * default methods. The implementation keeps Accessories' normal entries and
 * appends the Curios-backed Aether entries.
 */
@Mixin(AccessoriesCapabilityImpl.class)
public abstract class AetherAccessoriesBridgeMixin {

    @Shadow
    public abstract List<SlotEntryReference> getAllEquipped(boolean includeCosmetic);

    @Shadow
    public abstract net.minecraft.world.entity.LivingEntity entity();

    public SlotEntryReference getFirstEquipped(Predicate<ItemStack> predicate) {
        for (SlotEntryReference entry : getAllEquipped(true)) {
            if (predicate.test(entry.stack())) {
                return entry;
            }
        }

        for (var result : AetherCurioBridge.findAetherCurios(entity())) {
            if (!predicate.test(result.stack())) {
                continue;
            }

            Object entry = AetherCurioBridge.createAccessoriesEntry(result);
            if (entry instanceof SlotEntryReference slotEntry) {
                return slotEntry;
            }
        }

        return null;
    }

    public List<SlotEntryReference> getEquipped(Item item) {
        List<SlotEntryReference> result = new ArrayList<>();

        for (SlotEntryReference entry : getAllEquipped(true)) {
            if (entry.stack().getItem() == item) {
                result.add(entry);
            }
        }

        for (var slot : AetherCurioBridge.findAetherCurios(entity())) {
            if (slot.stack().getItem() != item) {
                continue;
            }

            Object entry = AetherCurioBridge.createAccessoriesEntry(slot);
            if (entry instanceof SlotEntryReference slotEntry) {
                result.add(slotEntry);
            }
        }

        return result;
    }

    public List<SlotEntryReference> getAllEquipped() {
        List<SlotEntryReference> result = new ArrayList<>(getAllEquipped(true));

        for (var slot : AetherCurioBridge.findAetherCurios(entity())) {
            Object entry = AetherCurioBridge.createAccessoriesEntry(slot);
            if (entry instanceof SlotEntryReference slotEntry) {
                result.add(slotEntry);
            }
        }

        return result;
    }
}
