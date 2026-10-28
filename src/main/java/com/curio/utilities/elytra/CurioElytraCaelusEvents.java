package com.curio.utilities.elytra;

import com.curio.utilities.CurioUtilities;
import com.illusivesoulworks.caelus.api.CaelusApi;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

/**
 * Pont Curios -> Caelus (Caelus est une dependance OBLIGATOIRE).
 *
 * Caelus decide si un joueur peut planer avec l'attribut fall_flying (valeur >= 1 = ALLOW).
 * Tant qu'une Elytra utilisable est dans le slot Curios "elytra", on ajoute un modificateur
 * transitoire de +1 sur cet attribut ; sinon on le retire.
 *
 * VERSION DIAGNOSTIC : ecrit dans le log des lignes "[CurioElytraDebug]" (une toutes les 2 s,
 * cote client ET serveur) pour savoir exactement ou la chaine s'arrete.
 * Mettre DEBUG = false (ou revenir a la version precedente) une fois le probleme trouve.
 */
@EventBusSubscriber(modid = CurioUtilities.MOD_ID)
public final class CurioElytraCaelusEvents {

    private static final boolean DEBUG = true;
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AttributeModifier CURIO_ELYTRA_MODIFIER = new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath(CurioUtilities.MOD_ID, "curio_elytra"),
            1.0D,
            AttributeModifier.Operation.ADD_VALUE
    );

    private static boolean firstEventLogged = false;

    private CurioElytraCaelusEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        String side = player.level().isClientSide ? "CLIENT" : "SERVER";

        if (DEBUG && !firstEventLogged) {
            firstEventLogged = true;
            LOGGER.info("[CurioElytraDebug] PlayerTickEvent.Post recu pour la premiere fois ({})", side);
        }

        AttributeInstance attribute = player.getAttribute(CaelusApi.getInstance().getFallFlyingAttribute());
        ItemStack equipped = CurioElytraCommon.getEquipped(player);
        boolean canFly = CurioElytraCommon.canFly(equipped, player);

        if (attribute != null) {
            boolean hasModifier = attribute.hasModifier(CURIO_ELYTRA_MODIFIER.id());

            if (canFly && !hasModifier) {
                attribute.addTransientModifier(CURIO_ELYTRA_MODIFIER);
            } else if (!canFly && hasModifier) {
                attribute.removeModifier(CURIO_ELYTRA_MODIFIER.id());
            }
        }

        if (DEBUG && player.tickCount % 40 == 0 && (!equipped.isEmpty() || (attribute != null && attribute.getValue() > 0.5D))) {
            LOGGER.info(
                    "[CurioElytraDebug] {} joueur={} curioElytra={} canFly={} attributNull={} modifierPresent={} valeurAttribut={} caelusTriState={} onGround={} fallFlying={}",
                    side,
                    player.getGameProfile().getName(),
                    equipped.isEmpty() ? "VIDE" : equipped.getItem(),
                    canFly,
                    attribute == null,
                    attribute != null && attribute.hasModifier(CURIO_ELYTRA_MODIFIER.id()),
                    attribute == null ? "n/a" : attribute.getValue(),
                    CaelusApi.getInstance().canFallFly(player),
                    player.onGround(),
                    player.isFallFlying()
            );
        }
    }
}
