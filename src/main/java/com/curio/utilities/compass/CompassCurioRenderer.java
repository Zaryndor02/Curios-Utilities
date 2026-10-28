package com.curio.utilities.compass;

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

public final class CompassCurioRenderer implements ICurioRenderer {

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

        boolean explorer = "explorers_compass".equals(slotContext.identifier());

        poseStack.pushPose();
        humanoid.body.translateAndRotate(poseStack);

        // The compass is worn on the top edge of the trousers, not on the torso
        // or near the hands.  We put one compass on each side of the hips so both
        // remain clearly visible in third person.
        // Positive X is the player's left side in the body model, negative X the right.
        float x = explorer ? -0.17F : 0.17F;
        float y = 0.48F;
        float z = -0.18F;

        // Keep the two compasses at the same belt/pants height while giving each
        // a slightly different angle so they look like two worn objects.
        float rotation = 0.0F;

        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        poseStack.scale(0.18F, 0.18F, 0.18F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                slotContext.entity(),
                stack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                buffer,
                slotContext.entity().level(),
                light,
                0,
                slotContext.index()
        );

        poseStack.popPose();
    }
}
