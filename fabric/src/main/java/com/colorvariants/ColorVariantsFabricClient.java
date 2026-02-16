package com.colorvariants;

import com.colorvariants.client.renderer.ColoredBlockRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class ColorVariantsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ColorVariants.COLORED_BLOCK_ENTITY.get(), ColoredBlockRenderer::new);
    }
}
