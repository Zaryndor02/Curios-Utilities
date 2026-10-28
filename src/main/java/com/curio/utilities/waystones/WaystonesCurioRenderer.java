package com.curio.utilities.waystones;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Rendu du Warp Stone sur le cote exterieur de la hanche/jambe gauche.
 */
public final class WaystonesCurioRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (stack.isEmpty() || !(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid)) {
            return;
        }

        LivingEntity entity = slotContext.entity();
        if (entity == null) {
            return;
        }

        // Si le joueur tient deja une Warp Stone, le rendu vanilla s'en charge.
        if (entity.getMainHandItem().is(stack.getItem()) || entity.getOffhandItem().is(stack.getItem())) {
            return;
        }

        poseStack.pushPose();

        // Ancrage DIRECT sur la jambe gauche : le pivot de la jambe correspond a la hanche.
        // Cela evite de placer la Warp Stone devant les deux boussoles.
        humanoid.rightLeg.translateAndRotate(poseStack);

        // Cote EXTERIEUR de la jambe gauche, au niveau de la hanche.
        // La zone visee est le cote exterieur visible a gauche de l image.
        // Avec le joueur vu de face, cette zone correspond au flanc exterieur de rightLeg.
        poseStack.translate(-0.14F, -0.02F, 0.00F);

        // La Warp Stone est tournee de 90 degres sur le cote.
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
        poseStack.scale(0.18F, 0.18F, 0.18F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity,
                stack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                buffer,
                entity.level(),
                light,
                0,
                slotContext.index()
        );

        poseStack.popPose();
    }
}
