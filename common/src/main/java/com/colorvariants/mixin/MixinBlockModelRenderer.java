package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;

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
        // Connection to client-side data storage is currently unimplemented (TODO).
        ColorTransform transform = ColorTransform.NONE; // TODO: get from client-side block entity

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
        int light = net.minecraft.client.renderer.LevelRenderer.getLightColor(level, pos);

        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone();

            // Apply color to each vertex
            for (int v = 0; v < 4; v++) {
                int colorIndex = v * 8 + 3;
                vertices[colorIndex] = transform.apply(vertices[colorIndex]);
            }

            // Re-create the quad with modified vertices to pass to consumer
            BakedQuad newQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

            consumer.putBulkData(poseStack.last(), newQuad, 1.0f, 1.0f, 1.0f, light, overlay);
        }
    }
}
