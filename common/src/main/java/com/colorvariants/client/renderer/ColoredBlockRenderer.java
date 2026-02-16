package com.colorvariants.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.TextureGenerator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/**
 * Custom renderer for colored blocks.
 */
public class ColoredBlockRenderer implements BlockEntityRenderer<ColoredBlockEntity> {

    private final TextureGenerator textureGenerator;
    private final BlockRenderDispatcher blockRenderer;

    public ColoredBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.textureGenerator = new TextureGenerator();
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
            ColoredBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int combinedLight,
            int combinedOverlay) {
        ColorTransform transform = blockEntity.getTransform();

        if (transform.isNone()) {
            return;
        }

        ResourceLocation coloredTexture = textureGenerator.getOrGenerateTexture(
                blockEntity.getBlockState(),
                transform);

        if (coloredTexture == null) {
            return;
        }

        // Render the block using the dynamic texture
        BakedModel model = blockRenderer.getBlockModel(blockEntity.getBlockState());
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(coloredTexture));

        blockRenderer.getModelRenderer().tesselateBlock(
                blockEntity.getLevel(),
                model,
                blockEntity.getBlockState(),
                blockEntity.getBlockPos(),
                poseStack,
                consumer,
                false,
                RandomSource.create(),
                blockEntity.getBlockState().getSeed(blockEntity.getBlockPos()),
                combinedOverlay);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
