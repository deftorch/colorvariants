package com.colorvariants.core;

import com.mojang.blaze3d.platform.NativeImage;
import com.colorvariants.util.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generates colored textures by applying ColorTransform to block textures.
 */
public class TextureGenerator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TextureGenerator.class);
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    
    private final TextureCache cache;
    private final TextureManager textureManager;
    
    public TextureGenerator() {
        this.cache = new TextureCache();
        this.textureManager = Minecraft.getInstance().getTextureManager();
    }
    
    /**
     * Gets or generates a colored texture for a block state.
     * 
     * @param blockState The block state
     * @param transform The color transform to apply
     * @return ResourceLocation of the colored texture
     */
    public ResourceLocation getOrGenerateTexture(BlockState blockState, ColorTransform transform) {
        // Check cache first
        String cacheKey = getCacheKey(blockState, transform);
        ResourceLocation cached = cache.get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        // Generate new texture
        return generateTexture(blockState, transform, cacheKey);
    }
    
    /**
     * Generates a colored texture asynchronously.
     */
    public CompletableFuture<ResourceLocation> generateTextureAsync(
        BlockState blockState, 
        ColorTransform transform
    ) {
        return CompletableFuture.supplyAsync(() -> {
            String cacheKey = getCacheKey(blockState, transform);
            return generateTexture(blockState, transform, cacheKey);
        }, EXECUTOR);
    }
    
    /**
     * Generates a colored texture synchronously.
     */
    private ResourceLocation generateTexture(
        BlockState blockState, 
        ColorTransform transform,
        String cacheKey
    ) {
        try {
            // Get original texture location
            ResourceLocation originalTexture = getBlockTexture(blockState);
            
            if (originalTexture == null) {
                LOGGER.warn("Could not find texture for block: {}", blockState.getBlock());
                return null;
            }
            
            // Load original texture
            NativeImage originalImage = loadTexture(originalTexture);
            
            if (originalImage == null) {
                LOGGER.warn("Could not load texture: {}", originalTexture);
                return null;
            }
            
            // Apply color transform
            NativeImage coloredImage = applyTransform(originalImage, transform);
            
            // Register as dynamic texture
            ResourceLocation coloredTexture = new ResourceLocation(
                "colorvariants",
                "generated/" + cacheKey
            );
            
            DynamicTexture dynamicTexture = new DynamicTexture(coloredImage);
            textureManager.register(coloredTexture, dynamicTexture);
            
            // Cache it
            cache.put(cacheKey, coloredTexture);
            
            // Clean up
            originalImage.close();
            
            return coloredTexture;
            
        } catch (Exception e) {
            LOGGER.error("Error generating texture", e);
            return null;
        }
    }
    
    /**
     * Applies color transform to an image.
     */
    private NativeImage applyTransform(NativeImage original, ColorTransform transform) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        NativeImage result = new NativeImage(width, height, true);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalColor = original.getPixelRGBA(x, y);
                
                // Skip fully transparent pixels
                int alpha = (originalColor >> 24) & 0xFF;
                if (alpha == 0) {
                    result.setPixelRGBA(x, y, originalColor);
                    continue;
                }
                
                // Apply transform
                int transformedColor = transform.apply(originalColor);
                result.setPixelRGBA(x, y, transformedColor);
            }
        }
        
        return result;
    }
    
    /**
     * Gets the texture location for a block state.
     */
    private ResourceLocation getBlockTexture(BlockState blockState) {
        try {
            // Get the block's model
            var modelManager = Minecraft.getInstance().getModelManager();
            var blockModel = modelManager.getBlockModelShaper()
                .getBlockModel(blockState);
            
            // Try to get sprite from particle texture (most reliable)
            var sprite = blockModel.getParticleIcon();
            
            if (sprite != null) {
                return sprite.contents().name();
            }
            
            return null;
            
        } catch (Exception e) {
            LOGGER.error("Error getting block texture", e);
            return null;
        }
    }
    
    /**
     * Loads a texture as a NativeImage.
     */
    private NativeImage loadTexture(ResourceLocation location) {
        try {
            var resourceManager = Minecraft.getInstance().getResourceManager();
            var resource = resourceManager.getResource(location);
            
            if (resource.isPresent()) {
                return NativeImage.read(resource.get().open());
            }
            
            return null;
            
        } catch (Exception e) {
            LOGGER.error("Error loading texture: " + location, e);
            return null;
        }
    }
    
    /**
     * Generates a cache key for a block state and transform.
     */
    private String getCacheKey(BlockState blockState, ColorTransform transform) {
        return String.format("%s_h%.0f_s%.2f_b%.2f",
            blockState.getBlock().toString().replace(":", "_"),
            transform.getHueShift(),
            transform.getSaturation(),
            transform.getBrightness()
        );
    }
    
    /**
     * Clears the texture cache.
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * Gets cache statistics.
     */
    public String getCacheStats() {
        return cache.getStats();
    }
    
    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        EXECUTOR.shutdown();
    }
}
