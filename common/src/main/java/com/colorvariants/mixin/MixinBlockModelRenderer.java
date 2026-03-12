package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
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
        ColorTransform transform = ColorTransform.NONE;

        if (level.getBlockEntity(pos) instanceof com.colorvariants.block.ColoredBlockEntity coloredBE) {
            transform = coloredBE.getTransform();
        }

        if (transform == null || transform.isNone()) return;

        ci.cancel();
        for (Direction dir : Direction.values()) {
            renderQuads(model.getQuads(state, dir, random), transform, level, state, pos, consumer, poseStack, overlay);
        }
        renderQuads(model.getQuads(state, null, random), transform, level, state, pos, consumer, poseStack, overlay);
    }

    private void renderQuads(List<BakedQuad> quads, ColorTransform transform,
                              BlockAndTintGetter level, BlockState state, BlockPos pos,
                              VertexConsumer consumer, PoseStack poseStack, int overlay) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone();

            // Apply color transformations
            for (int v = 0; v < 4; v++) {
                int vertexColor = vertices[v * 8 + 3];
                // In Minecraft 1.20.1 the color is in ARGB format when dealing with VertexConsumer directly or inside BakedQuad?
                // BakedQuad colors are usually ABGR in little endian if I recall correctly, but `apply` expects ARGB and packs it in ARGB.
                // Let's swap the bytes if necessary, but actually `apply` deals with whatever format if we just pass ARGB.
                // It's safer to use the exact `multiplyColors` logic as mentioned in AGENTS.md, but let's just apply the transform.

                // BakedQuad format (DefaultVertexFormat.BLOCK):
                // position: 3 floats (12 bytes) -> 3 ints
                // color: 4 bytes -> 1 int
                // uv: 2 floats (8 bytes) -> 2 ints
                // lightmap: 2 shorts (4 bytes) -> 1 int
                // normal: 3 bytes + padding (4 bytes) -> 1 int
                // total 8 ints per vertex.

                // Color in vertex array is generally A B G R or A R G B.
                // Let's just use `apply` for now. Wait, `apply` takes ARGB.
                // Is the quad color ARGB or ABGR? Let's just pass it to `apply`.
                // In 1.20, it's typically ABGR.
                // Let's decode ABGR:
                int a = (vertexColor >> 24) & 0xFF;
                int b = (vertexColor >> 16) & 0xFF;
                int g = (vertexColor >> 8) & 0xFF;
                int r = vertexColor & 0xFF;

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                int transformedArgb = transform.apply(argb);

                int ta = (transformedArgb >> 24) & 0xFF;
                int tr = (transformedArgb >> 16) & 0xFF;
                int tg = (transformedArgb >> 8) & 0xFF;
                int tb = transformedArgb & 0xFF;

                vertices[v * 8 + 3] = (ta << 24) | (tb << 16) | (tg << 8) | tr;
            }

            int light = net.minecraft.client.renderer.LevelRenderer.getLightColor(level, state, pos);

            // To pass it down to putBulkData, I need to look up its signature.
            // putBulkData is typically `putBulkData(PoseStack.Pose, BakedQuad, float[], float, float, float, int[], int, boolean)`
            // Wait, memory says: "int baseColor = transform.toARGB(); ... consumer.putBulkData(...)"
            // I will use `consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, light, overlay);`
            // Let's just put the modified quad vertices manually or see if there's a putBulkData for int array?
            // Usually we can't easily change the quad's vertex array and pass it to putBulkData without creating a new BakedQuad.

            BakedQuad coloredQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

            float r = 1.0f;
            float g = 1.0f;
            float b = 1.0f;

            if (quad.isTinted()) {
                int tintIndex = quad.getTintIndex();
                int tintColor = net.minecraft.client.Minecraft.getInstance().getBlockColors().getColor(state, level, pos, tintIndex);
                r = (tintColor >> 16 & 255) / 255.0F;
                g = (tintColor >> 8 & 255) / 255.0F;
                b = (tintColor & 255) / 255.0F;
            }

            consumer.putBulkData(poseStack.last(), coloredQuad, r, g, b, light, overlay);
        }
    }
}
