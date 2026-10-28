package com.curio.utilities.elytra;

import com.curio.utilities.CurioUtilities;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = CurioUtilities.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public final class CurioElytraClientEvents {

    private CurioElytraClientEvents() {
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            addPlayerLayer(event, skin);
        }

        addEntityLayer(event, EntityType.ARMOR_STAND);
    }

    private static void addPlayerLayer(
            EntityRenderersEvent.AddLayers event,
            PlayerSkin.Model skin
    ) {
        EntityRenderer<?> renderer = event.getSkin(skin);

        if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            addLayer(livingRenderer, event);
        }
    }

    private static void addEntityLayer(
            EntityRenderersEvent.AddLayers event,
            EntityType<?> entityType
    ) {
        EntityRenderer<?> renderer = event.getRenderer(entityType);

        if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            addLayer(livingRenderer, event);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayer(
            LivingEntityRenderer renderer,
            EntityRenderersEvent.AddLayers event
    ) {
        renderer.addLayer(new CurioElytraRenderLayer(renderer, event.getEntityModels()));
    }
}
