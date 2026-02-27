package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

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
        // TODO: Connection to client-side data storage is currently unimplemented
        // ColorTransform transform = ColorTransformManager.get((Level) level).getTransform(pos);
        ColorTransform transform = ColorTransform.NONE;
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

            for (int v = 0; v < 4; v++) {
                int colorIndex = v * 8 + 3;
                int originalColor = vertices[colorIndex];

                // If the quad has a tint index, we need to blend it with the block tint
                int tintColor = 0xFFFFFFFF;
                if (quad.isTinted()) {
                    tintColor = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, quad.getTintIndex());
                    tintColor = tintColor | 0xFF000000;
                }

                // Apply our HSV transform
                int modifiedColor = transform.apply(multiplyColors(originalColor, tintColor));
                vertices[colorIndex] = modifiedColor;
            }

            float[] brightnessTable = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
            int light = LevelRenderer.getLightColor(level, state, pos);
            int[] lightmap = new int[]{light, light, light, light};

            BakedQuad newQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

            consumer.putBulkData(poseStack.last(), newQuad, brightnessTable, 1.0f, 1.0f, 1.0f, lightmap, overlay, true);
        }
    }

    private int multiplyColors(int color1, int color2) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return (a1 << 24) | (r << 16) | (g << 8) | b;
    }
}
