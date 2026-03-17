package com.colorvariants.mixin;

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

import com.colorvariants.core.ColorTransform;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.colorvariants.block.ColoredBlockEntity;
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
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ColoredBlockEntity coloredBE)) return;

        ColorTransform transform = coloredBE.getTransform();
        if (transform.isNone()) return;

        ci.cancel();

        // Implement according to vanilla BlockModelRenderer but mapped and modified quads
        int baseColor = transform.apply(0xFFFFFFFF);

        // Use Minecraft's own method to render models smoothly or flat, but since we cancel
        // the best approach in 1.20.1 to avoid missing AO and Culling when cancelling is using the level's
        // standard BlockModelRenderer instance to render a *modified* model, OR wrapping the VertexConsumer.
        // But since we can't easily wrap consumer here without rewriting the whole pipeline,
        // let's do exactly what AGENTS.md showed BUT including the `checkSides` logic to fix culling!

        // Grab lighting dynamically
        int packedLight = net.minecraft.client.renderer.LevelRenderer.getLightColor(level, state, pos);
        int[] lights = new int[]{packedLight, packedLight, packedLight, packedLight};

        // dummy float array, standard for flat lighting
        float[] brightness = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

        for (Direction dir : Direction.values()) {
            if (!checkSides || net.minecraft.world.level.block.Block.shouldRenderFace(state, level, pos, dir, pos.relative(dir))) {
                random.setSeed(seed);
                List<BakedQuad> quads = model.getQuads(state, dir, random);
                for (BakedQuad quad : quads) {
                    renderModifiedQuad(quad, baseColor, poseStack, consumer, brightness, lights, overlay);
                }
            }
        }

        random.setSeed(seed);
        List<BakedQuad> generalQuads = model.getQuads(state, null, random);
        for (BakedQuad quad : generalQuads) {
            renderModifiedQuad(quad, baseColor, poseStack, consumer, brightness, lights, overlay);
        }
    }

    private void renderModifiedQuad(BakedQuad quad, int baseColor, PoseStack poseStack, VertexConsumer consumer, float[] brightness, int[] lights, int overlay) {
        int[] vertices = quad.getVertices().clone();
        for (int v = 0; v < 4; v++) {
            vertices[v * 8 + 3] = multiplyColors(vertices[v * 8 + 3], baseColor);
        }

        BakedQuad coloredQuad = new BakedQuad(
            vertices,
            quad.getTintIndex(),
            quad.getDirection(),
            quad.getSprite(),
            quad.isTinted()
        );

        // Use putBulkData (PoseStack.Pose, BakedQuad, float[], float, float, float, int[], int, boolean)
        consumer.putBulkData(poseStack.last(), coloredQuad, brightness, 1.0f, 1.0f, 1.0f, lights, overlay, true);
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
