package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.block.ColoredBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.resources.model.BakedModel;

@Mixin(ModelBlockRenderer.class)
public class MixinBlockModelRenderer {

    // A simpler and safer way is to inject at `tesselateBlock` HEAD, capture the arguments,
    // and if there's a ColorTransform, we use ThreadLocal to pass it down.
    // However, since we need to modify the vertices of BakedQuad *before* it gets put,
    // intercepting the Quads returned by `model.getQuads()` is the cleanest approach.
    // Or we could use a custom vertex consumer wrapper.

    // But since modifying BakedQuad arrays directly creates garbage, it's better to wrap VertexConsumer.
    // However, the instructions say:
    // "Implement vertex coloring mixin according to AGENTS.md part 'Rendering Implementation'."
    // AGENTS.md shows cancelling `tesselateBlock` and re-implementing.
    // But the code review said that cancelling `tesselateBlock` breaks face culling and AO.
    // To respect AGENTS.md AND the code review, we will inject at `tesselateBlock`,
    // NOT cancel it, but instead wrap the VertexConsumer!
    // Wait, AGENTS.md specifically provided the snippet:
    // @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    // ...
    // ci.cancel();
    //
    // Wait, the review said: "The patch introduces major regressions. By injecting at HEAD, cancelling the original tesselateBlock method, and replacing it with an oversimplified custom rendering loop, it breaks critical Minecraft rendering pipelines... The mixin should ideally modify the quad colors dynamically without completely rewriting the underlying block rendering logic, or accurately replicate the AO and culling mechanisms."

    // Let's modify the `VertexConsumer` passed to `tesselateBlock`.
    // We can use @ModifyVariable on `consumer` parameter in `tesselateBlock`
    // and pass a wrapped VertexConsumer that multiplies the colors!

    @org.spongepowered.asm.mixin.injection.ModifyVariable(
        method = "tesselateBlock",
        at = @At("HEAD"),
        argsOnly = true,
        index = 6 // VertexConsumer consumer is the 6th arg (1 level, 2 model, 3 state, 4 pos, 5 poseStack, 6 consumer)
    )
    private VertexConsumer colorvariants$wrapConsumer(
        VertexConsumer consumer, BlockAndTintGetter level, BakedModel model, BlockState state,
        BlockPos pos, PoseStack poseStack, VertexConsumer consumerArg,
        boolean checkSides, RandomSource random, long seed, int overlay
    ) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ColoredBlockEntity coloredBE) {
            ColorTransform transform = coloredBE.getTransform();
            if (!transform.isNone()) {
                return new ColoredVertexConsumer(consumer, transform);
            }
        }
        return consumer;
    }

    private static class ColoredVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int colorMultiplier;

        public ColoredVertexConsumer(VertexConsumer delegate, ColorTransform transform) {
            this.delegate = delegate;
            this.colorMultiplier = transform.apply(0xFFFFFFFF);
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            // Apply the color multiplier to the incoming color
            int multA = (colorMultiplier >> 24) & 0xFF;
            int multR = (colorMultiplier >> 16) & 0xFF;
            int multG = (colorMultiplier >> 8) & 0xFF;
            int multB = colorMultiplier & 0xFF;

            r = (r * multR) / 255;
            g = (g * multG) / 255;
            b = (b * multB) / 255;
            a = (a * multA) / 255;

            delegate.color(r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            delegate.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }

        @Override
        public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, int[] lightmap, int overlay, boolean readColor) {
            // putBulkData writes vertices directly, bypassing the single-element methods
            // We need to modify the quad or use the single-element fallbacks if modifying quad is slow.
            // Wrapping putBulkData is possible by duplicating its default logic but calling our color() method.
            // The default implementation in VertexConsumer decomposes the quad.

            // To simplify, we can just let it fall back to the default implementation of putBulkData
            // inside VertexConsumer which calls our overridden vertex(), color(), uv(), etc.
            // Wait, does VertexConsumer.putBulkData have a default method that calls our overrides?
            // Yes! According to VertexConsumer source, `putBulkData` is a default method.
            // So if we don't override it, it will call the default method which will call `putBulkData(Pose, BakedQuad, ...)`
            // Wait, we MUST override `putBulkData` to intercept it, because `delegate` might be a subclass that overrides it
            // and skips our `color()` hook!

            // Let's modify the BakedQuad here and pass it to the delegate's putBulkData to keep the fast path!
            int[] vertices = quad.getVertices().clone();
            for (int v = 0; v < 4; v++) {
                vertices[v * 8 + 3] = multiplyColors(vertices[v * 8 + 3], colorMultiplier);
            }
            BakedQuad modifiedQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());

            delegate.putBulkData(pose, modifiedQuad, brightness, red, green, blue, lightmap, overlay, readColor);
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
            int a = (a1 * a2) / 255;

            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
}
