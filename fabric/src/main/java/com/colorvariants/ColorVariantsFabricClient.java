package com.colorvariants;

import net.fabricmc.api.ClientModInitializer;

public class ColorVariantsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Rendering is now handled via MixinBlockModelRenderer
    }
}
