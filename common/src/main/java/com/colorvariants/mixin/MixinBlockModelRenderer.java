package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
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
        ColorTransform transform = ColorTransformManager.get(level).getTransform(pos);
        if (!transform.isNone()) {
            ci.cancel();
            // Render all directions including null (general quads)
            for (Direction dir : Direction.values()) {
                colorvariants$renderQuads(model.getQuads(state, dir, random), transform, level, state, pos, consumer, poseStack, overlay);
            }
            colorvariants$renderQuads(model.getQuads(state, null, random), transform, level, state, pos, consumer, poseStack, overlay);
        }
    }

    @Unique
    private void colorvariants$renderQuads(List<BakedQuad> quads, ColorTransform transform,
                                           BlockAndTintGetter level, BlockState state, BlockPos pos,
                                           VertexConsumer consumer, PoseStack poseStack, int overlay) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone(); // NEVER mutate original
            for (int v = 0; v < 4; v++) {
                // Vertex format usually has color at index 3 (DefaultVertexFormat.BLOCK)
                // Format: x, y, z, color, u, v, ...
                int colorIndex = 3;
                if (vertices.length > v * 8 + colorIndex) {
                     vertices[v * 8 + colorIndex] = transform.apply(vertices[v * 8 + colorIndex]);
                }
            }

            // Create a new Quad with modified vertices
            BakedQuad newQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

            // Calculate packed light
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            int skyLight = level.getBrightness(LightLayer.SKY, pos);
            int packedLight = LightTexture.pack(blockLight, skyLight);

            consumer.putBulkData(poseStack.last(), newQuad, 1.0f, 1.0f, 1.0f, packedLight, overlay);
        }
    }
}
