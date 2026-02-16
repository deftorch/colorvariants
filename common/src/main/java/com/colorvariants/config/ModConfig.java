package com.colorvariants.config;

/**
 * Configuration options for the Color Variants mod.
 */
public class ModConfig {

    // Configuration values are accessed lazily to prevent early access issues on Forge
    public static boolean isCacheEnabled() {
        return com.colorvariants.platform.Services.CONFIG.isCacheEnabled();
    }

    public static int getMaxCacheSize() {
        return com.colorvariants.platform.Services.CONFIG.getMaxCacheSize();
    }

    public static boolean isAsyncGenerationEnabled() {
        return com.colorvariants.platform.Services.CONFIG.isAsyncGenerationEnabled();
    }

    public static int MAX_COLORED_BLOCKS_PER_CHUNK = 0; // Unused in interface for now

    // TODO: Implement multi-loader config system
    public static void init() {
        // No-op for now
    }
}
