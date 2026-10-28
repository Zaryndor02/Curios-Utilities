package com.curio.utilities.elytra;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.entity.ItemRenderer;

/**
 * Elytra rendering copied in behavior from Elytra Slot, using Minecraft's
 * normal ElytraModel so wing animation follows the player exactly.
 */
public final class CurioElytraRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private final ElytraModel<T> elytraModel;

    public CurioElytraRenderLayer(
            RenderLayerParent<T, M> parent,
            EntityModelSet modelSet
    ) {
        super(parent);
        this.elytraModel = new ElytraModel<>(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        ItemStack stack = CurioElytraCommon.getEquipped(entity);

        if (stack.isEmpty() || stack.getItem() instanceof ArmorItem) {
            return;
        }

        ResourceLocation texture = CurioElytraRenderInfo.getTexture(stack);

        if (entity instanceof AbstractClientPlayer player) {
            PlayerSkin skin = player.getSkin();

            if (skin.elytraTexture() != null) {
                texture = skin.elytraTexture();
            } else if (CurioElytraRenderInfo.useCapeTexture(stack)) {
                ResourceLocation customCape =
                        CurioElytraCapeHelper.getCustomCape(player);

                if (customCape != null) {
                    texture = customCape;
                } else if (skin.capeTexture() != null
                        && player.isModelPartShown(
                        net.minecraft.world.entity.player.PlayerModelPart.CAPE
                )) {
                    texture = skin.capeTexture();
                }
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.125D);

        getParentModel().copyPropertiesTo(elytraModel);

        elytraModel.setupAnim(
                entity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch
        );

        var vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                buffer,
                RenderType.armorCutoutNoCull(texture),
                stack.isEnchanted()
        );

        elytraModel.renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF
        );

        poseStack.popPose();
    }
}