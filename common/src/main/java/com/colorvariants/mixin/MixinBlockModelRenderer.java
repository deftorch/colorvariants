package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
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
        // As per memory: Client-side rendering logic must retrieve color data from the local BlockEntity (e.g., level.getBlockEntity(pos)), as server-side SavedData managers (like ColorTransformManager) are not accessible on the client.
        // Wait, the memory also states: MixinBlockModelRenderer retrieves block tint using Minecraft.getInstance().getBlockColors(). Connection to client-side data storage is currently unimplemented (TODO).
        // Let's use ColorTransform.NONE for now and put a TODO. The memory says: "MixinBlockModelRenderer includes a guard clause if (transform.isNone()) return; effectively disabling custom rendering until a client-side data source is fully implemented to prevent glitches."

        ColorTransform transform = ColorTransform.NONE; // TODO: Fetch from client-side block entity

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

            // To apply the transform, we can apply it to a base white color, or directly multiply
            // But ColorTransform doesn't have a toARGB(). It has apply(int rgb).
            // The memory mentions "int baseColor = transform.toARGB();" as an example in AGENTS.md, but that method doesn't exist.
            // Let's just use the logic from AGENTS.md modified to work with apply() on white color.
            int baseColor = transform.apply(0xFFFFFFFF);

            for (int v = 0; v < 4; v++) {
                vertices[v * 8 + 3] = multiplyColors(vertices[v * 8 + 3], baseColor);
            }

            // consumer.putBulkData takes different parameters in 1.20.1
            // public void putBulkData(PoseStack.Pose p_85996_, BakedQuad p_85997_, float p_85998_, float p_85999_, float p_86000_, int p_86001_, int p_86002_)
            consumer.putBulkData(
                poseStack.last(),
                new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade()),
                1.0f, 1.0f, 1.0f,
                net.minecraft.client.renderer.LevelRenderer.getLightColor(level, state, pos), // getLightColor requires state in 1.20.1
                overlay
            );
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

        int a = (a1 * a2) / 255;
        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
