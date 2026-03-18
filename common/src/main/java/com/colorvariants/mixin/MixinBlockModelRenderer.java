package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
        // TODO: Client-side data connection
        // For now, get color data from local BlockEntity if it exists, since ColorTransformManager requires ServerLevel
        ColorTransform transform = ColorTransform.NONE;
        if (level.getBlockEntity(pos) != null) {
            // we will need to access the color from the block entity,
            // but for now we just use NONE to bypass if not found
        }
        if (transform.isNone()) return;

        ci.cancel();
        // Render all directions including null (general quads)
        for (Direction dir : Direction.values()) {
            renderQuads(model.getQuads(state, dir, random), transform, level, state, pos, consumer, poseStack, overlay);
        }
        renderQuads(model.getQuads(state, null, random), transform, level, state, pos, consumer, poseStack, overlay);
    }

    @Unique
    private void renderQuads(List<BakedQuad> quads, ColorTransform transform,
                             BlockAndTintGetter level, BlockState state, BlockPos pos,
                             VertexConsumer consumer, PoseStack poseStack, int overlay) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone(); // NEVER mutate original

            // To properly color the vertex, we iterate over the 4 vertices (8 ints per vertex usually in default format)
            // Color is at offset 3 for DefaultVertexFormat.BLOCK
            for (int v = 0; v < 4; v++) {
                int colorIndex = v * 8 + 3;
                int originalColor = vertices[colorIndex];
                vertices[colorIndex] = transform.apply(originalColor);
            }

            // Create a new quad with modified vertices to properly pass it to consumer
            BakedQuad coloredQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

            float[] brightnesses = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
            int light = net.minecraft.client.renderer.LevelRenderer.getLightColor(level, state, pos);
            int[] lights = new int[]{light, light, light, light};

            consumer.putBulkData(poseStack.last(), coloredQuad, brightnesses, 1.0F, 1.0F, 1.0F, lights, overlay, false);
        }
    }

}