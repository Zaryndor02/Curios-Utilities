package com.curio.utilities.elytra;

import com.curio.utilities.CurioUtilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;

/**
 * Enregistre CurioElytra (capability Curios) pour chaque Elytra reconnue.
 *
 * RegisterCapabilitiesEvent est un evenement du bus de MOD. Avec @EventBusSubscriber sans
 * "bus", FML abonne au bus de JEU (Bus.GAME par defaut) : l'evenement n'y arrive jamais, donc
 * CurioElytra (curioTick = durabilite, canEquip, son) n'etait jamais actif.
 */
@EventBusSubscriber(modid = CurioUtilities.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CurioElytraModEvents {

    private CurioElytraModEvents() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();

            if (!CurioElytraCommon.isElytra(stack)) {
                continue;
            }

            event.registerItem(
                    CuriosCapability.ITEM,
                    (itemStack, context) -> new CurioElytra(itemStack),
                    item
            );
        }
    }
}
