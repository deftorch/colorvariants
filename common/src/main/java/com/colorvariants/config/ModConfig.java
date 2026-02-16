package com.colorvariants.config;

/**
 * Configuration options for the Color Variants mod.
 */
public class ModConfig {

    public static boolean CACHE_ENABLED = com.colorvariants.platform.Services.CONFIG.isCacheEnabled();
    public static int MAX_CACHE_SIZE = com.colorvariants.platform.Services.CONFIG.getMaxCacheSize();
    public static boolean ASYNC_GENERATION = com.colorvariants.platform.Services.CONFIG.isAsyncGenerationEnabled();
    public static int MAX_COLORED_BLOCKS_PER_CHUNK = 0; // Unused in interface for now

    // TODO: Implement multi-loader config system
    public static void init() {
        // No-op for now
    }
}
