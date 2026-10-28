package com.curio.utilities;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.gui.GuiWrapper;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.curio.utilities.network.RequestExplorersCompassSyncPacket;
import com.curio.utilities.network.StopExplorersCompassPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Integration CLIENT d'Explorer's Compass.
 *
 * Aucune annotation d'evenement ici : c'est CurioUtilitiesClient /
 * CurioUtilitiesClientModEvents qui appellent ces methodes, seulement si Explorer's Compass
 * est charge. Ne jamais referencer cette classe sans ce test.
 *
 * Touches : K = ouvrir la GUI, U = arreter la recherche (H et J sont pris par Nature's
 * Compass). Elles sont modifiables dans Options > Controles.
 *
 * Le HUD reproduit celui d'ExplorersCompassOverlay (etats, lignes, couleurs, distance :
 * lus dans son bytecode), car l'overlay officiel ne lit que les mains du joueur.
 */
public class ExplorersCompassClient {

    public static final KeyMapping SEARCH_EXPLORERS_COMPASS = new KeyMapping(
            "key.curioutilities.search_explorers_compass",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.curioutilities"
    );

    public static final KeyMapping STOP_EXPLORERS_COMPASS = new KeyMapping(
            "key.curioutilities.stop_explorers_compass",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "key.categories.curioutilities"
    );

    // Meme principe que pour Nature's Compass : une demande de sync par session de jeu.
    private static boolean syncRequestedThisSession = false;

    // Couleurs de l'overlay officiel : 16777215 = blanc, 11184810 = gris.
    private static final int COLOR_WHITE = 0xFFFFFF;
    private static final int COLOR_GRAY = 0xAAAAAA;

    // Si un HUD de Nature's Compass est deja affiche, on se place en dessous.
    private static final int LINE_OFFSET_WHEN_NATURES_COMPASS_VISIBLE = 12;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SEARCH_EXPLORERS_COMPASS);
        event.register(STOP_EXPLORERS_COMPASS);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            syncRequestedThisSession = false;
            return;
        }

        // Des que la boussole est dans le slot Curios, on demande au serveur les donnees
        // d'Explorer's Compass (liste des structures, groupes, etc.), une fois par session.
        // Explorer's Compass ne les envoie lui-meme que dans use() (clic droit en main).
        if (!syncRequestedThisSession && !findCurioExplorersCompass(minecraft.player).isEmpty()) {
            syncRequestedThisSession = true;
            PacketDistributor.sendToServer(new RequestExplorersCompassSyncPacket());
        }

        if (minecraft.screen != null) {
            return;
        }

        while (SEARCH_EXPLORERS_COMPASS.consumeClick()) {
            openGui(minecraft);
        }

        while (STOP_EXPLORERS_COMPASS.consumeClick()) {
            stopExplorersCompass(minecraft);
        }
    }

    private static void openGui(Minecraft minecraft) {
        Player player = minecraft.player;
        ItemStack compass = findCurioExplorersCompass(player);
        if (!compass.isEmpty()) {
            // Si la reponse du serveur n'est pas encore arrivee, l'ecran d'Explorer's Compass
            // recharge tout seul sa liste a son arrivee (ExplorersCompassScreen.tick lit
            // ExplorersCompass.synced).
            GuiWrapper.openGUI(minecraft.level, player, compass);
        }
    }

    private static void stopExplorersCompass(Minecraft minecraft) {
        ItemStack compass = findCurioExplorersCompass(minecraft.player);
        if (!compass.isEmpty()) {
            PacketDistributor.sendToServer(new StopExplorersCompassPacket());
        }
    }

    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Memes garde-fous que ExplorersCompassOverlay.render().
        if (player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        if (mc.getDebugOverlay().showDebugScreen()) {
            return;
        }

        if (mc.screen != null) {
            if (!ConfigHandler.CLIENT.displayWithChatOpen.get() || !(mc.screen instanceof ChatScreen)) {
                return;
            }
        }

        // Boussole en main : c'est l'overlay officiel qui dessine, pas nous (pas de double HUD).
        if (player.getMainHandItem().getItem() instanceof ExplorersCompassItem
                || player.getOffhandItem().getItem() instanceof ExplorersCompassItem) {
            return;
        }

        ItemStack stack = findCurioExplorersCompass(player);
        if (stack.isEmpty() || !(stack.getItem() instanceof ExplorersCompassItem compass)) {
            return;
        }

        CompassState state = compass.getState(stack);

        if (state != CompassState.SEARCHING
                && state != CompassState.FOUND
                && state != CompassState.NOT_FOUND) {
            return;
        }

        int offset = CompassIntegrations.naturesCompassLoaded() && NaturesCompassClient.isHudVisible(player)
                ? LINE_OFFSET_WHEN_NATURES_COMPASS_VISIBLE
                : 0;

        GuiGraphics graphics = event.getGuiGraphics();

        if (state == CompassState.SEARCHING) {
            line(graphics, I18n.get("string.explorerscompass.status"), COLOR_WHITE, 0 + offset);
            line(graphics, I18n.get("string.explorerscompass.searching"), COLOR_GRAY, 1 + offset);

            line(graphics, I18n.get("string.explorerscompass.structure"), COLOR_WHITE, 3 + offset);
            line(graphics, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), COLOR_GRAY, 4 + offset);

            line(graphics, I18n.get("string.explorerscompass.radius"), COLOR_WHITE, 6 + offset);
            line(graphics, String.valueOf(compass.getSearchRadius(stack)), COLOR_GRAY, 7 + offset);
        } else if (state == CompassState.FOUND) {
            line(graphics, I18n.get("string.explorerscompass.status"), COLOR_WHITE, 0 + offset);
            line(graphics, I18n.get("string.explorerscompass.found"), COLOR_GRAY, 1 + offset);

            line(graphics, I18n.get("string.explorerscompass.structure"), COLOR_WHITE, 3 + offset);
            line(graphics, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), COLOR_GRAY, 4 + offset);

            if (compass.shouldDisplayCoordinates(stack)) {
                int x = compass.getFoundStructureX(stack);
                int z = compass.getFoundStructureZ(stack);

                line(graphics, I18n.get("string.explorerscompass.coordinates"), COLOR_WHITE, 6 + offset);
                line(graphics, x + ", " + z, COLOR_GRAY, 7 + offset);

                line(graphics, I18n.get("string.explorerscompass.distance"), COLOR_WHITE, 9 + offset);
                line(graphics, String.valueOf(StructureUtils.getHorizontalDistanceToLocation(player, x, z)), COLOR_GRAY, 10 + offset);
            }
        } else {
            // NOT_FOUND
            line(graphics, I18n.get("string.explorerscompass.status"), COLOR_WHITE, 0 + offset);
            line(graphics, I18n.get("string.explorerscompass.notFound"), COLOR_GRAY, 1 + offset);

            line(graphics, I18n.get("string.explorerscompass.structure"), COLOR_WHITE, 3 + offset);
            line(graphics, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), COLOR_GRAY, 4 + offset);

            line(graphics, I18n.get("string.explorerscompass.radius"), COLOR_WHITE, 6 + offset);
            line(graphics, String.valueOf(compass.getSearchRadius(stack)), COLOR_GRAY, 7 + offset);
        }
    }

    private static void line(GuiGraphics graphics, String text, int color, int line) {
        RenderUtils.drawConfiguredStringOnHUD(graphics, text, 5, 5, color, line);
    }

    public static ItemStack findCurioExplorersCompass(Player player) {
        return CurioExplorerCompassHelper.find(player);
    }
}
