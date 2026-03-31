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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ColoredBlockEntity)) return;

        ColorTransform transform = ((ColoredBlockEntity) be).getTransform();
        if (transform == null || transform.isNone()) return;

        ci.cancel();
        // Render all directions including null (general quads)
        for (Direction dir : Direction.values()) {
            if (!checkSides || Block.shouldRenderFace(state, level, pos, dir, pos.relative(dir))) {
                renderQuads(model.getQuads(state, dir, random), transform, level, state, pos, consumer, poseStack, overlay);
            }
        }
        renderQuads(model.getQuads(state, null, random), transform, level, state, pos, consumer, poseStack, overlay);
    }

    private void renderQuads(List<BakedQuad> quads, ColorTransform transform,
                              BlockAndTintGetter level, BlockState state, BlockPos pos,
                              VertexConsumer consumer, PoseStack poseStack, int overlay) {
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices().clone(); // NEVER mutate original

            for (int v = 0; v < 4; v++) {
                int origColor = vertices[v * 8 + 3];
                // Apply the transform to the vertex color
                vertices[v * 8 + 3] = transform.apply(origColor);
            }

            int lightColor = LevelRenderer.getLightColor(level, pos);

            consumer.putBulkData(
                poseStack.last(),
                new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade()),
                new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                1.0f, 1.0f, 1.0f,
                new int[]{lightColor, lightColor, lightColor, lightColor},
                overlay,
                true
            );
        }
    }
}
