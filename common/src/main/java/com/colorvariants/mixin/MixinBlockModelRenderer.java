package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelBlockRenderer.class)
public class MixinBlockModelRenderer {

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    public void colorvariants$renderColoredBlock(
        BlockAndTintGetter level, BakedModel model, BlockState state,
        BlockPos pos, PoseStack poseStack, VertexConsumer consumer,
        boolean checkSides, RandomSource random, long seed, int overlay,
        CallbackInfo ci
    ) {
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof com.colorvariants.block.ColoredBlockEntity)) return;
        ColorTransform transform = ((com.colorvariants.block.ColoredBlockEntity) be).getTransform();
        if (transform.isNone()) return;

        ci.cancel();
        // Render all directions including null (general quads)
        for (Direction dir : Direction.values()) {
            renderQuads(model.getQuads(state, dir, random), transform, level, state, pos, consumer, poseStack, overlay);
        }
        renderQuads(model.getQuads(state, null, random), transform, level, state, pos, consumer, poseStack, overlay);
    }

    private void renderQuads(List<BakedQuad> quads, ColorTransform transform,
                             BlockAndTintGetter level, BlockState state, BlockPos pos,
                             VertexConsumer consumer, PoseStack poseStack, int overlay) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone(); // NEVER mutate original
            int baseColor = transform.apply(0xFFFFFFFF);
            for (int v = 0; v < 4; v++) {
                vertices[v * 8 + 3] = multiplyColors(vertices[v * 8 + 3], baseColor);
            }
            float[] brightness = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
            int[] lights = new int[]{LevelRenderer.getLightColor(level, pos), LevelRenderer.getLightColor(level, pos), LevelRenderer.getLightColor(level, pos), LevelRenderer.getLightColor(level, pos)};
            consumer.putBulkData(poseStack.last(), quad, brightness, 1.0f, 1.0f, 1.0f, lights, overlay, true);
        }
    }

    private int multiplyColors(int color1, int color2) {
        int a = ((color1 >> 24) & 0xFF) * ((color2 >> 24) & 0xFF) / 255;
        int r = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 255;
        int g = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 255;
        int b = (color1 & 0xFF) * (color2 & 0xFF) / 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
