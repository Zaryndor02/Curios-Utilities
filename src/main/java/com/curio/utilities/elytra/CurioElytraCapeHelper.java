package com.curio.utilities.elytra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * Optional compatibility with MinecraftCapes, matching Elytra Slot's behavior
 * without making MinecraftCapes a dependency of Curio Utilities.
 */
public final class CurioElytraCapeHelper {

    private CurioElytraCapeHelper() {
    }

    public static ResourceLocation getCustomCape(Player player) {
        if (!ModList.get().isLoaded("minecraftcapes")) {
            return null;
        }

        try {
            Class<?> playerHandlerClass = Class.forName("net.minecraftcapes.player.PlayerHandler");
            Method getHandler = playerHandlerClass.getMethod("get", Player.class);
            Object handler = getHandler.invoke(null, player);

            if (handler == null) {
                return null;
            }

            Method getCapeLocation = playerHandlerClass.getMethod("getCapeLocation");
            Object cape = getCapeLocation.invoke(handler);

            if (!(cape instanceof ResourceLocation capeLocation)) {
                return null;
            }

            Class<?> configClass = Class.forName("net.minecraftcapes.config.MinecraftCapesConfig");
            Method isCapeVisible = configClass.getMethod("isCapeVisible");
            Object visible = isCapeVisible.invoke(null);

            return Boolean.TRUE.equals(visible) ? capeLocation : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }
}
