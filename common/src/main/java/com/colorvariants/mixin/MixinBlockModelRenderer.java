package com.colorvariants.mixin;

import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.core.ColorTransform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        ordinal = 0
    )
    private VertexConsumer colorvariants$wrapVertexConsumer(VertexConsumer originalConsumer,
        BlockAndTintGetter level, BakedModel model, BlockState state,
        BlockPos pos, PoseStack poseStack, VertexConsumer consumer,
        boolean checkSides, RandomSource random, long seed, int overlay) {

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ColoredBlockEntity)) return originalConsumer;

        ColorTransform transform = ((ColoredBlockEntity) blockEntity).getTransform();
        if (transform.isNone()) return originalConsumer;

        return new com.colorvariants.client.renderer.ColoredVertexConsumer(originalConsumer, transform);
    }
}
