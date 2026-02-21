package com.colorvariants.mixin;

import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.core.ColorTransform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        // Retrieve color from BlockEntity
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ColoredBlockEntity coloredBE)) {
            return;
        }

        ColorTransform transform = coloredBE.getTransform();
        if (transform.isNone()) {
            return;
        }

        ci.cancel();

        // Render all directions including null (general quads)
        for (Direction dir : Direction.values()) {
            random.setSeed(seed);
            List<BakedQuad> quads = model.getQuads(state, dir, random);
            if (!quads.isEmpty()) {
                renderQuads(quads, transform, level, state, pos, consumer, poseStack, overlay);
            }
        }

        random.setSeed(seed);
        List<BakedQuad> quads = model.getQuads(state, null, random);
        if (!quads.isEmpty()) {
            renderQuads(quads, transform, level, state, pos, consumer, poseStack, overlay);
        }
    }

    @Unique
    private void renderQuads(List<BakedQuad> quads, ColorTransform transform,
                              BlockAndTintGetter level, BlockState state, BlockPos pos,
                              VertexConsumer consumer, PoseStack poseStack, int overlay) {
        PoseStack.Pose entry = poseStack.last();

        // Calculate light (basic, no AO)
        int light = LevelRenderer.getLightColor(level, state, pos);

        for (BakedQuad quad : quads) {
            // Handle tint
            float r = 1.0f;
            float g = 1.0f;
            float b = 1.0f;
            if (quad.isTinted()) {
                int tint = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, quad.getTintIndex());
                r = (float)(tint >> 16 & 255) / 255.0F;
                g = (float)(tint >> 8 & 255) / 255.0F;
                b = (float)(tint & 255) / 255.0F;
            }

            int[] vertices = quad.getVertices().clone(); // NEVER mutate original
            int transformColor = transform.toARGB();

            // Apply transform color to each vertex
            for (int v = 0; v < 4; v++) {
                int colorIndex = v * 8 + 3;
                if (colorIndex < vertices.length) {
                    vertices[colorIndex] = multiplyColors(vertices[colorIndex], transformColor);
                }
            }

            // Create new quad with modified vertices
            BakedQuad newQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

            // Put modified quad
            consumer.putBulkData(
                entry,
                newQuad,
                r, g, b, // Tint color
                light,
                overlay
            );
        }
    }

    @Unique
    private int multiplyColors(int color1, int color2) {
        // Unpack ARGB
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = (color1) & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = (color2) & 0xFF;

        // Multiply
        int a = (a1 * a2) / 255;
        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        // Repack ARGB
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
