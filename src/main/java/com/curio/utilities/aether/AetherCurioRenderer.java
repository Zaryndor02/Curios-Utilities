package com.curio.utilities.aether;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Adapts an Aether Accessories renderer so that Curios can render the same
 * visual model when the Aether item is stored in one of our Curios slots.
 *
 * Accessories remains a runtime implementation detail: no Accessories class
 * is referenced directly by the Curio Utilities source code.
 */
public final class AetherCurioRenderer implements ICurioRenderer {

    private final String rendererClassName;
    private Object aetherRenderer;
    private Method renderMethod;

    public AetherCurioRenderer(String rendererClassName) {
        this.rendererClassName = rendererClassName;
    }

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
        try {
            ensureRenderer();

            Object reference = AetherCurioBridge.createSlotReference(
                    slotContext.entity(),
                    AetherCurioBridge.CURIO_TO_ACCESSORIES.get(slotContext.identifier()),
                    slotContext.index()
            );

            if (reference == null) {
                return;
            }

            renderMethod.invoke(
                    aetherRenderer,
                    stack,
                    reference,
                    poseStack,
                    renderLayerParent.getModel(),
                    buffer,
                    light,
                    limbSwing,
                    limbSwingAmount,
                    partialTicks,
                    ageInTicks,
                    netHeadYaw,
                    headPitch
            );
        } catch (Throwable ignored) {
            // Aether is an optional integration. Never let a renderer failure
            // break the Curio Utilities client.
        }
    }

    private void ensureRenderer() throws Exception {
        if (aetherRenderer != null && renderMethod != null) {
            return;
        }

        Class<?> rendererClass = Class.forName(rendererClassName);
        Constructor<?> constructor = rendererClass.getConstructor();
        aetherRenderer = constructor.newInstance();

        Class<?> slotReferenceClass = Class.forName(
                "io.wispforest.accessories.api.slot.SlotReference"
        );

        renderMethod = rendererClass.getMethod(
                "render",
                ItemStack.class,
                slotReferenceClass,
                PoseStack.class,
                EntityModel.class,
                MultiBufferSource.class,
                int.class,
                float.class,
                float.class,
                float.class,
                float.class,
                float.class,
                float.class
        );
    }
}
