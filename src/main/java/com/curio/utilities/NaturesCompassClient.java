package com.curio.utilities;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.gui.GuiWrapper;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.CompassState;
import com.chaosthedude.naturescompass.util.RenderUtils;
import com.curio.utilities.network.RequestNaturesCompassSyncPacket;
import com.curio.utilities.network.StopNaturesCompassPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Integration CLIENT de Nature's Compass. C'est l'ancien contenu de CurioUtilitiesClient,
 * deplace tel quel (touches H / J, demande de synchronisation, ouverture du GUI, HUD).
 *
 * Aucune annotation d'evenement ici : c'est CurioUtilitiesClient /
 * CurioUtilitiesClientModEvents qui appellent ces methodes, seulement si Nature's Compass
 * est charge. Ne jamais referencer cette classe sans ce test.
 *
 * Differences avec l'ancienne version : (1) classe separee, (2) le HUD n'est plus dessine
 * apres chaque couche d'interface mais une seule fois (filtre dans CurioUtilitiesClient),
 * (3) isHudVisible() pour que le HUD d'Explorer's Compass se decale en dessous.
 */
public class NaturesCompassClient {

    public static final KeyMapping SEARCH_NATURES_COMPASS = new KeyMapping(
            "key.curioutilities.search_natures_compass",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.curioutilities"
    );

    public static final KeyMapping STOP_NATURES_COMPASS = new KeyMapping(
            "key.curioutilities.stop_natures_compass",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.curioutilities"
    );

    // Vrai une fois la demande de synchronisation envoyee pour la session de jeu
    // en cours. Remis a faux quand le joueur n'existe plus (deconnexion, changement de monde).
    private static boolean syncRequestedThisSession = false;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SEARCH_NATURES_COMPASS);
        event.register(STOP_NATURES_COMPASS);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            syncRequestedThisSession = false;
            return;
        }

        // Des que la boussole est dans le slot Curios, on demande au serveur
        // les donnees de Nature's Compass (liste des biomes, etc.), une seule fois par session.
        // Nature's Compass ne les envoie lui-meme que dans use() (clic droit en main).
        if (!syncRequestedThisSession && !findCurioNaturesCompass(minecraft.player).isEmpty()) {
            syncRequestedThisSession = true;
            PacketDistributor.sendToServer(new RequestNaturesCompassSyncPacket());
        }

        if (minecraft.screen != null) {
            return;
        }

        while (SEARCH_NATURES_COMPASS.consumeClick()) {
            openGui(minecraft);
        }

        while (STOP_NATURES_COMPASS.consumeClick()) {
            stopNaturesCompass(minecraft);
        }
    }

    private static void openGui(Minecraft minecraft) {
        Player player = minecraft.player;
        ItemStack compass = findCurioNaturesCompass(player);
        if (!compass.isEmpty()) {
            // Si la reponse du serveur n'est pas encore arrivee, l'ecran de
            // Nature's Compass recharge tout seul la liste des biomes a son arrivee
            // (NaturesCompassScreen.tick lit NaturesCompass.synced).
            GuiWrapper.openGUI(minecraft.level, player, compass);
        }
    }

    private static void stopNaturesCompass(Minecraft minecraft) {
        ItemStack compass = findCurioNaturesCompass(minecraft.player);
        if (!compass.isEmpty()) {
            PacketDistributor.sendToServer(new StopNaturesCompassPacket());
        }
    }

    /**
     * Vrai si un HUD de Nature's Compass est affiche en ce moment : celui du mod
     * (boussole en main) ou le notre (boussole dans Curios).
     */
    public static boolean isHudVisible(Player player) {
        if (player == null) {
            return false;
        }

        ItemStack held = ItemStack.EMPTY;
        if (player.getMainHandItem().getItem() instanceof NaturesCompassItem) {
            held = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof NaturesCompassItem) {
            held = player.getOffhandItem();
        }

        ItemStack stack = held.isEmpty() ? findCurioNaturesCompass(player) : held;

        if (stack.isEmpty() || !(stack.getItem() instanceof NaturesCompassItem compass)) {
            return false;
        }

        if (compass.getState(stack) == CompassState.INACTIVE) {
            return false;
        }

        if (!held.isEmpty()) {
            return true;
        }

        String biomeIdStr = stack.get(NaturesCompass.BIOME_ID);
        return biomeIdStr != null && !biomeIdStr.isEmpty();
    }

    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        if (player.getMainHandItem().getItem() instanceof NaturesCompassItem
                || player.getOffhandItem().getItem() instanceof NaturesCompassItem) {
            return;
        }

        ItemStack stack = findCurioNaturesCompass(player);
        if (!stack.isEmpty() && stack.getItem() instanceof NaturesCompassItem compass) {

            String biomeIdStr = stack.get(NaturesCompass.BIOME_ID);
            CompassState state = compass.getState(stack);

            if (biomeIdStr == null || biomeIdStr.isEmpty() || state == CompassState.INACTIVE) {
                return;
            }

            int colorWhite = 0xFFFFFFFF;
            int colorGray = 0xFFAAAAAA;

            if (state == CompassState.SEARCHING) {
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.status"), 5, 5, colorWhite, 0);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.searching"), 5, 5, colorGray, 1);

                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.biome"), 5, 5, colorWhite, 3);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), BiomeUtils.getBiomeName(mc.level, ResourceLocation.parse(biomeIdStr)), 5, 5, colorGray, 4);

                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.radius"), 5, 5, colorWhite, 6);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), String.valueOf(stack.getOrDefault(NaturesCompass.SEARCH_RADIUS, 0)), 5, 5, colorGray, 7);
            } else if (state == CompassState.FOUND) {
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.status"), 5, 5, colorWhite, 0);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.found"), 5, 5, colorGray, 1);

                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.biome"), 5, 5, colorWhite, 3);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), BiomeUtils.getBiomeName(mc.level, ResourceLocation.parse(biomeIdStr)), 5, 5, colorGray, 4);

                if (stack.getOrDefault(NaturesCompass.DISPLAY_COORDS, false)) {
                    RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.coordinates"), 5, 5, colorWhite, 6);
                    RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), stack.getOrDefault(NaturesCompass.FOUND_X, 0) + ", " + stack.getOrDefault(NaturesCompass.FOUND_Z, 0), 5, 5, colorGray, 7);

                    RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.distance"), 5, 5, colorWhite, 9);
                    RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), String.valueOf(BiomeUtils.getDistanceToBiome(player, stack.getOrDefault(NaturesCompass.FOUND_X, 0), stack.getOrDefault(NaturesCompass.FOUND_Z, 0))), 5, 5, colorGray, 10);
                }
            } else if (state == CompassState.NOT_FOUND) {
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.status"), 5, 5, colorWhite, 0);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.notFound"), 5, 5, colorGray, 1);

                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.biome"), 5, 5, colorWhite, 3);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), BiomeUtils.getBiomeName(mc.level, ResourceLocation.parse(biomeIdStr)), 5, 5, colorGray, 4);

                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.radius"), 5, 5, colorWhite, 6);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), String.valueOf(stack.getOrDefault(NaturesCompass.SEARCH_RADIUS, 0)), 5, 5, colorGray, 7);

                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), I18n.get("string.naturescompass.samples"), 5, 5, colorWhite, 9);
                RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), String.valueOf(stack.getOrDefault(NaturesCompass.SAMPLES, 0)), 5, 5, colorGray, 10);
            }
        }
    }

    public static ItemStack findCurioNaturesCompass(Player player) {
        return CurioCompassHelper.find(player);
    }
}
