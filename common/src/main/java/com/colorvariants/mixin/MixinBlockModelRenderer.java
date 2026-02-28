package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.BakedModel;
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
        // NOTE: Connection to client-side data storage is currently unimplemented.
        // We will retrieve data from BlockEntity in the future.
        // Currently falling back to Minecraft.getInstance().getBlockColors() tint check or stub logic
        // until client data layer is finalized.
        // As per memory, use guard clause `if (transform.isNone()) return;`

        // For now, since server-side manager is inaccessible on client, and we don't have
        // the block entity logic, we'll try to fetch from block entity. If not available, we return.

        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        ColorTransform transform = ColorTransform.NONE;

        // TODO: Implement actual data retrieval from BE when ColoredBlockEntity is rewritten
        // Until then, this acts as a stub guard.

        if (transform.isNone()) {
            return;
        }

        ci.cancel();
        // Render all directions including null (general quads)
        for (Direction dir : Direction.values()) {
            colorvariants$renderQuads(model.getQuads(state, dir, random), transform, level, state, pos, consumer, poseStack, overlay);
        }
        colorvariants$renderQuads(model.getQuads(state, null, random), transform, level, state, pos, consumer, poseStack, overlay);
    }

    @Unique
    private void colorvariants$renderQuads(List<BakedQuad> quads, ColorTransform transform,
                              BlockAndTintGetter level, BlockState state, BlockPos pos,
                              VertexConsumer consumer, PoseStack poseStack, int overlay) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone(); // NEVER mutate original
            // Apply transform to white base color to get the target ARGB color
            int baseColor = transform.apply(0xFFFFFFFF);

            // Get block tint
            int tint = -1;
            if (quad.isTinted()) {
                tint = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, quad.getTintIndex());
            }

            // Get light color
            int light = net.minecraft.client.renderer.LevelRenderer.getLightColor(level, state, pos);

            for (int v = 0; v < 4; v++) {
                // Blend tint and baseColor
                int color = vertices[v * 8 + 3];
                if (tint != -1) {
                    color = colorvariants$multiplyColors(color, tint);
                }
                vertices[v * 8 + 3] = colorvariants$multiplyColors(color, baseColor);
            }

            // Reconstruct the quad and put it into the consumer
            consumer.putBulkData(poseStack.last(), new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade()), 1.0f, 1.0f, 1.0f, light, overlay);
        }
    }

    @Unique
    private int colorvariants$multiplyColors(int c1, int c2) {
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (a1 * a2) / 255;
        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
