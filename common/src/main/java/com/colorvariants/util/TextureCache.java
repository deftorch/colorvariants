package com.colorvariants.util;

import com.colorvariants.config.ModConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache for generated textures to improve performance.
 */
public class TextureCache {

    private final Map<String, ResourceLocation> cache = new LinkedHashMap<String, ResourceLocation>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ResourceLocation> eldest) {
            return size() > getMaxCacheSize();
        }
    };

    /**
     * Gets a texture from the cache.
     */
    public ResourceLocation get(String key) {
        if (!isCacheEnabled()) {
            return null;
        }
        return cache.get(key);
    }

    /**
     * Puts a texture in the cache.
     */
    public void put(String key, ResourceLocation texture) {
        if (isCacheEnabled()) {
            cache.put(key, texture);
        }
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Gets the cache size.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Checks if the cache contains a key.
     */
    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    /**
     * Gets cache statistics.
     */
    public String getStats() {
        return String.format("TextureCache: %d/%d entries",
                cache.size(),
                getMaxCacheSize());
    }

    private boolean isCacheEnabled() {
        return ModConfig.CACHE_ENABLED;
    }

    private int getMaxCacheSize() {
        return ModConfig.MAX_CACHE_SIZE;
    }
}
