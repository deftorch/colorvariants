package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.colorvariants.block.ColoredBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelBlockRenderer.class)
public class MixinBlockModelRenderer {

    @ModifyVariable(
            method = "tesselateBlock",
            at = @At("HEAD"),
            argsOnly = true,
            index = 6
    )
    public VertexConsumer colorvariants$wrapVertexConsumer(VertexConsumer consumer, BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack) {
        if (!(level.getBlockEntity(pos) instanceof ColoredBlockEntity coloredBlock)) {
            return consumer;
        }

        ColorTransform transform = coloredBlock.getTransform();
        if (transform.isNone()) return consumer;

        return new VertexConsumer() {
            @Override
            public VertexConsumer vertex(double x, double y, double z) {
                return consumer.vertex(x, y, z);
            }

            @Override
            public VertexConsumer color(int r, int g, int b, int a) {
                int argbColor = (a << 24) | (r << 16) | (g << 8) | b;
                int newArgb = transform.apply(argbColor);

                int newR = (newArgb >> 16) & 0xFF;
                int newG = (newArgb >> 8) & 0xFF;
                int newB = newArgb & 0xFF;
                int newA = (newArgb >> 24) & 0xFF;

                return consumer.color(newR, newG, newB, newA);
            }

            @Override
            public VertexConsumer uv(float u, float v) {
                return consumer.uv(u, v);
            }

            @Override
            public VertexConsumer overlayCoords(int u, int v) {
                return consumer.overlayCoords(u, v);
            }

            @Override
            public VertexConsumer uv2(int u, int v) {
                return consumer.uv2(u, v);
            }

            @Override
            public VertexConsumer normal(float x, float y, float z) {
                return consumer.normal(x, y, z);
            }

            @Override
            public void endVertex() {
                consumer.endVertex();
            }

            @Override
            public void defaultColor(int r, int g, int b, int a) {
                int argbColor = (a << 24) | (r << 16) | (g << 8) | b;
                int newArgb = transform.apply(argbColor);

                int newR = (newArgb >> 16) & 0xFF;
                int newG = (newArgb >> 8) & 0xFF;
                int newB = newArgb & 0xFF;
                int newA = (newArgb >> 24) & 0xFF;
                consumer.defaultColor(newR, newG, newB, newA);
            }

            @Override
            public void unsetDefaultColor() {
                consumer.unsetDefaultColor();
            }

            @Override
            public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float red, float green, float blue, int combinedLight, int combinedOverlay) {
                int[] vertices = quad.getVertices().clone();

                for (int v = 0; v < 4; v++) {
                    int colorIndex = v * 8 + 3;
                    int vertexColor = vertices[colorIndex];

                    int r = vertexColor & 0xFF;
                    int g = (vertexColor >> 8) & 0xFF;
                    int b = (vertexColor >> 16) & 0xFF;
                    int a = (vertexColor >> 24) & 0xFF;

                    int argbColor = (a << 24) | (r << 16) | (g << 8) | b;
                    int newArgb = transform.apply(argbColor);

                    int newR = (newArgb >> 16) & 0xFF;
                    int newG = (newArgb >> 8) & 0xFF;
                    int newB = newArgb & 0xFF;
                    int newA = (newArgb >> 24) & 0xFF;

                    int newVertexColor = (newA << 24) | (newB << 16) | (newG << 8) | newR;
                    vertices[colorIndex] = newVertexColor;
                }

                consumer.putBulkData(poseEntry, new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade()), red, green, blue, combinedLight, combinedOverlay);
            }

            @Override
            public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] brightness, float red, float green, float blue, int[] lights, int overlay, boolean readExistingColor) {
                int[] vertices = quad.getVertices().clone();

                for (int v = 0; v < 4; v++) {
                    int colorIndex = v * 8 + 3;
                    int vertexColor = vertices[colorIndex];

                    int r = vertexColor & 0xFF;
                    int g = (vertexColor >> 8) & 0xFF;
                    int b = (vertexColor >> 16) & 0xFF;
                    int a = (vertexColor >> 24) & 0xFF;

                    int argbColor = (a << 24) | (r << 16) | (g << 8) | b;
                    int newArgb = transform.apply(argbColor);

                    int newR = (newArgb >> 16) & 0xFF;
                    int newG = (newArgb >> 8) & 0xFF;
                    int newB = newArgb & 0xFF;
                    int newA = (newArgb >> 24) & 0xFF;

                    int newVertexColor = (newA << 24) | (newB << 16) | (newG << 8) | newR;
                    vertices[colorIndex] = newVertexColor;
                }

                consumer.putBulkData(poseEntry, new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade()), brightness, red, green, blue, lights, overlay, readExistingColor);
            }
        };
    }
}
