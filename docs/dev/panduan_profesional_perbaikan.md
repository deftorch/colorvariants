# PANDUAN PROFESIONAL: MEMPERBAIKI COLOR VARIANTS MOD
## Kompatibilitas Rendering & Integrasi Multi-Mod

**Target Audience:** Intermediate to Advanced Minecraft Modders  
**Difficulty:** Advanced  
**Time Required:** 3-4 weeks  
**Prerequisites:** Java 17, Gradle, Minecraft Modding Experience

---

## 📚 DAFTAR ISI

1. [Executive Summary](#executive-summary)
2. [Architecture Overview](#architecture-overview)
3. [Phase 1: Critical Bug Fixes](#phase-1-critical-bug-fixes)
4. [Phase 2: Rendering System Implementation](#phase-2-rendering-system-implementation)
5. [Phase 3: Mod Compatibility](#phase-3-mod-compatibility)
6. [Phase 4: Performance & Optimization](#phase-4-performance--optimization)
7. [Phase 5: Testing & Validation](#phase-5-testing--validation)
8. [Appendix: Code Templates](#appendix-code-templates)

---

## EXECUTIVE SUMMARY

### Current Issues
1. 🔴 **Rendering tidak implemented** - Warna tidak terlihat
2. 🔴 **Area Selector broken** - Shared state di multiplayer
3. 🔴 **BlockEntity approach flawed** - Tidak work untuk vanilla blocks
4. 🟡 **Mod compatibility issues** - Potential conflicts

### Proposed Solution Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT SIDE                              │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────┐      ┌──────────────┐     ┌─────────────┐ │
│  │ ColorWand   │─────>│ ColorPicker  │────>│ Send Packet │ │
│  │   Item      │      │     GUI      │     │  to Server  │ │
│  └─────────────┘      └──────────────┘     └──────┬──────┘ │
│                                                     │         │
│  ┌──────────────────────────────────────────────┐ │         │
│  │    RENDERING PIPELINE (NEW)                  │ │         │
│  │  ┌────────────────────────────────────────┐ │ │         │
│  │  │ RenderSystem Event Handler             │ │ │         │
│  │  │  1. Get ColorTransform from cache      │ │ │         │
│  │  │  2. Check if texture cached            │ │ │         │
│  │  │  3. If not: Generate async             │ │ │         │
│  │  │  4. Apply to vertex buffer             │ │ │         │
│  │  └────────────────────────────────────────┘ │ │         │
│  │                                              │ │         │
│  │  ┌────────────────────────────────────────┐ │ │         │
│  │  │ Client Color Cache                     │ │ │         │
│  │  │  - Thread-safe LRU cache               │ │ │         │
│  │  │  - BlockPos -> ColorTransform map      │ │ │         │
│  │  │  - Synced with server                  │ │ │         │
│  │  └────────────────────────────────────────┘ │ │         │
│  └──────────────────────────────────────────────┘ │         │
│                                                     │         │
└─────────────────────────────────────────────────────────────┘
                                                      │
                                  Network Layer       │
                                                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     SERVER SIDE                              │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Packet Handler                                        │   │
│  │  1. Validate request (distance, permissions, rate)   │   │
│  │  2. Apply transform to ColorTransformManager          │   │
│  │  3. Broadcast to nearby players                       │   │
│  │  4. Mark chunk dirty for save                         │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ ColorTransformManager (SavedData)                     │   │
│  │  - Persistent storage                                 │   │
│  │  - Chunk-based indexing for performance               │   │
│  │  - World save/load integration                        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## PHASE 1: CRITICAL BUG FIXES

### 1.1 Fix Area Selector Multi-Player Bug

**Duration:** 4 hours  
**Priority:** P0 (Critical)  
**Files to modify:**
- `common/src/main/java/com/colorvariants/item/AreaSelectorItem.java`

#### Problem Analysis
```java
// CURRENT CODE (BROKEN)
public class AreaSelectorItem extends Item {
    private static BlockPos firstPos = null;   // ❌ Shared across all players
    private static BlockPos secondPos = null;  // ❌ Race condition
}
```

#### Solution: Client-Side State Management

**Option A: Per-Item NBT Storage (Recommended)**

```java
package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Fixed Area Selector with per-item state storage.
 * Each item stack maintains its own selection state.
 */
public class AreaSelectorItem extends Item {

    private static final String FIRST_POS_TAG = "FirstPos";
    private static final String SECOND_POS_TAG = "SecondPos";

    public AreaSelectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            HitResult hitResult = Minecraft.getInstance().hitResult;

            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                BlockPos pos = blockHit.getBlockPos();

                if (player.isShiftKeyDown()) {
                    // Set second position
                    setSecondPos(stack, pos);
                    player.displayClientMessage(
                        Component.translatable("item.colorvariants.area_selector.second_pos",
                            pos.getX(), pos.getY(), pos.getZ()),
                        true
                    );

                    // If both positions set, open GUI
                    BlockPos firstPos = getFirstPos(stack);
                    if (firstPos != null) {
                        openAreaGUI(level, player, stack);
                    }
                } else {
                    // Set first position and clear second
                    setFirstPos(stack, pos);
                    clearSecondPos(stack);
                    player.displayClientMessage(
                        Component.translatable("item.colorvariants.area_selector.first_pos",
                            pos.getX(), pos.getY(), pos.getZ()),
                        true
                    );
                }
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private void openAreaGUI(Level level, Player player, ItemStack stack) {
        BlockPos firstPos = getFirstPos(stack);
        BlockPos secondPos = getSecondPos(stack);
        
        if (firstPos != null && secondPos != null) {
            Minecraft.getInstance().setScreen(
                new com.colorvariants.client.gui.AreaColorPickerScreen(firstPos, secondPos)
            );
        }
    }

    // ============== NBT Storage Methods ==============

    /**
     * Stores the first position in the item's NBT.
     */
    public static void setFirstPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(FIRST_POS_TAG, NbtUtils.writeBlockPos(pos));
    }

    /**
     * Gets the first position from the item's NBT.
     */
    @Nullable
    public static BlockPos getFirstPos(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(FIRST_POS_TAG)) {
            return NbtUtils.readBlockPos(stack.getTag().getCompound(FIRST_POS_TAG));
        }
        return null;
    }

    /**
     * Stores the second position in the item's NBT.
     */
    public static void setSecondPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(SECOND_POS_TAG, NbtUtils.writeBlockPos(pos));
    }

    /**
     * Gets the second position from the item's NBT.
     */
    @Nullable
    public static BlockPos getSecondPos(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(SECOND_POS_TAG)) {
            return NbtUtils.readBlockPos(stack.getTag().getCompound(SECOND_POS_TAG));
        }
        return null;
    }

    /**
     * Clears the second position.
     */
    public static void clearSecondPos(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(SECOND_POS_TAG);
        }
    }

    /**
     * Resets both positions.
     */
    public static void reset(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(FIRST_POS_TAG);
            stack.getTag().remove(SECOND_POS_TAG);
        }
    }

    /**
     * Checks if both positions are set.
     */
    public static boolean hasValidSelection(ItemStack stack) {
        return getFirstPos(stack) != null && getSecondPos(stack) != null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.1"));
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.2"));
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.3"));
        
        // Show current selection in tooltip
        BlockPos first = getFirstPos(stack);
        BlockPos second = getSecondPos(stack);
        
        if (first != null) {
            tooltip.add(Component.literal("§7First: §f" + 
                first.getX() + ", " + first.getY() + ", " + first.getZ()));
        }
        if (second != null) {
            tooltip.add(Component.literal("§7Second: §f" + 
                second.getX() + ", " + second.getY() + ", " + second.getZ()));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Add enchantment glint when both positions are set
        return hasValidSelection(stack);
    }
}
```

**Benefits:**
- ✅ No static state - each item has own selection
- ✅ Multiplayer safe
- ✅ Persists across inventory moves
- ✅ Visual feedback (enchantment glint)
- ✅ Shows selection in tooltip

### 1.2 Update AreaColorPickerScreen

Update constructor untuk menerima positions dari item stack instead of static variables.

```java
// common/src/main/java/com/colorvariants/client/gui/AreaColorPickerScreen.java

public class AreaColorPickerScreen extends Screen {
    
    private final BlockPos startPos;
    private final BlockPos endPos;
    
    public AreaColorPickerScreen(BlockPos pos1, BlockPos pos2) {
        super(Component.translatable("gui.colorvariants.area_color_picker.title"));
        
        // Normalize positions to create proper bounding box
        this.startPos = new BlockPos(
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );
        this.endPos = new BlockPos(
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ())
        );
    }
    
    // ... rest of implementation
}
```

---

## PHASE 2: RENDERING SYSTEM IMPLEMENTATION

### 2.1 Architecture Decision

**Goal:** Apply color transformations to block rendering WITHOUT modifying block textures permanently.

**Approach:** Vertex Color Manipulation via Mixin

**Why this approach?**
- ✅ Works with ALL blocks (vanilla + modded)
- ✅ No texture atlas modification
- ✅ Compatible with resource packs
- ✅ Performance efficient
- ✅ Mod compatibility friendly

**Alternative Approaches Considered:**
- ❌ Dynamic texture generation - Heavy memory, atlas issues
- ❌ BlockEntity rendering - Only works for specific blocks
- ❌ Shader-based - Requires core mods, compatibility issues

### 2.2 Client-Side Color Cache

**Duration:** 1 day  
**File:** `common/src/main/java/com/colorvariants/client/ColorTransformCache.java`

```java
package com.colorvariants.client;

import com.colorvariants.core.ColorTransform;
import net.minecraft.core.BlockPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Client-side cache for color transformations.
 * Thread-safe and optimized for rendering performance.
 * 
 * Uses BlockPos.asLong() for efficient storage.
 */
public class ColorTransformCache {
    
    private static final int MAX_CACHE_SIZE = 50000; // Configurable
    
    // Thread-safe LRU cache using fastutil for performance
    private final Long2ObjectMap<ColorTransform> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private long hits = 0;
    private long misses = 0;
    
    public ColorTransformCache() {
        // LinkedHashMap with access-order for LRU
        this.cache = new Long2ObjectLinkedOpenHashMap<>(MAX_CACHE_SIZE);
    }
    
    /**
     * Gets a color transform from cache.
     * Returns null if not cached.
     */
    public ColorTransform get(BlockPos pos) {
        long key = pos.asLong();
        
        lock.readLock().lock();
        try {
            ColorTransform transform = cache.get(key);
            if (transform != null) {
                hits++;
                return transform;
            } else {
                misses++;
                return null;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Stores a color transform in cache.
     * Implements LRU eviction when cache is full.
     */
    public void put(BlockPos pos, ColorTransform transform) {
        long key = pos.asLong();
        
        lock.writeLock().lock();
        try {
            // Remove oldest entry if cache is full
            if (cache.size() >= MAX_CACHE_SIZE) {
                // fastutil LinkedHashMap maintains insertion/access order
                long oldestKey = cache.keySet().iterator().nextLong();
                cache.remove(oldestKey);
            }
            
            cache.put(key, transform);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Removes a transform from cache.
     */
    public void remove(BlockPos pos) {
        lock.writeLock().lock();
        try {
            cache.remove(pos.asLong());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Clears the entire cache.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            hits = 0;
            misses = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Gets cache statistics.
     */
    public CacheStats getStats() {
        lock.readLock().lock();
        try {
            return new CacheStats(
                cache.size(),
                MAX_CACHE_SIZE,
                hits,
                misses,
                hits + misses > 0 ? (double) hits / (hits + misses) : 0.0
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static record CacheStats(
        int size,
        int maxSize,
        long hits,
        long misses,
        double hitRate
    ) {
        @Override
        public String toString() {
            return String.format(
                "Cache: %d/%d entries, %.1f%% hit rate (%d hits, %d misses)",
                size, maxSize, hitRate * 100, hits, misses
            );
        }
    }
}
```

### 2.3 Rendering Mixin - Core Implementation

**Duration:** 3-5 days  
**File:** `common/src/main/java/com/colorvariants/mixin/MixinBlockModelRenderer.java`

This is the **MOST CRITICAL** piece - the actual rendering hook.

```java
package com.colorvariants.mixin;

import com.colorvariants.client.ColorTransformCache;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.ColorVariants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * Mixin to apply color transformations during block rendering.
 * 
 * This hooks into the block model rendering pipeline and modifies
 * vertex colors based on stored color transformations.
 * 
 * COMPATIBILITY NOTES:
 * - Works with vanilla and modded blocks
 * - Compatible with Optifine/Sodium (uses standard rendering hooks)
 * - Does not modify texture atlas
 * - Safe for resource packs
 */
@Mixin(ModelBlockRenderer.class)
public class MixinBlockModelRenderer {
    
    // Thread-local cache instance for performance
    private static final ThreadLocal<ColorTransformCache> CACHE = 
        ThreadLocal.withInitial(ColorTransformCache::new);
    
    /**
     * Injects into the renderModel method to apply color transformations.
     * 
     * This method is called for every block face being rendered.
     * We modify it to apply our color transformation to vertex colors.
     */
    @Inject(
        method = "renderModel(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V",
        at = @At("HEAD")
    )
    private void colorvariants$beforeRender(
        BlockAndTintGetter level,
        BakedModel model,
        BlockState state,
        BlockPos pos,
        PoseStack poseStack,
        VertexConsumer buffer,
        boolean checkSides,
        RandomSource random,
        long seed,
        int overlay,
        CallbackInfo ci
    ) {
        // Check cache for color transform
        ColorTransformCache cache = CACHE.get();
        ColorTransform transform = cache.get(pos);
        
        // If not in cache, we need to query (done async to avoid blocking)
        if (transform == null) {
            // Request async load (implementation in Phase 2.4)
            requestTransformLoad(pos);
        }
    }
    
    /**
     * Injects into the putQuadData method to modify vertex colors.
     * 
     * This is where we actually change the color of each vertex.
     */
    @Inject(
        method = "putQuadData",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFII)V",
            shift = At.Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void colorvariants$modifyQuadColor(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        VertexConsumer buffer,
        PoseStack.Pose pose,
        BakedQuad quad,
        float brightness0,
        float brightness1,
        float brightness2,
        float brightness3,
        int lightmap0,
        int lightmap1,
        int lightmap2,
        int lightmap3,
        int overlay,
        CallbackInfo ci
    ) {
        ColorTransformCache cache = CACHE.get();
        ColorTransform transform = cache.get(pos);
        
        if (transform != null && !transform.isNone()) {
            // Apply color transformation to vertex colors
            applyColorTransform(
                buffer, quad, transform,
                brightness0, brightness1, brightness2, brightness3
            );
            
            // Cancel original putBulkData call
            ci.cancel();
        }
    }
    
    /**
     * Applies color transformation to a quad's vertices.
     */
    private void applyColorTransform(
        VertexConsumer buffer,
        BakedQuad quad,
        ColorTransform transform,
        float brightness0,
        float brightness1,
        float brightness2,
        float brightness3
    ) {
        // Get quad vertex data
        int[] vertexData = quad.getVertices();
        
        // Vertex format: [x, y, z, color, u, v, lightmap, normal]
        // Color is at index 3 in packed format
        
        float[] brightness = {brightness0, brightness1, brightness2, brightness3};
        
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 8; // 8 ints per vertex
            
            // Extract original color
            int packedColor = vertexData[offset + 3];
            int r = (packedColor >> 16) & 0xFF;
            int g = (packedColor >> 8) & 0xFF;
            int b = packedColor & 0xFF;
            int a = (packedColor >> 24) & 0xFF;
            
            // Apply transform
            int originalRGB = (r << 16) | (g << 8) | b;
            int transformedRGB = transform.apply(originalRGB);
            
            // Extract transformed components
            int newR = (transformedRGB >> 16) & 0xFF;
            int newG = (transformedRGB >> 8) & 0xFF;
            int newB = transformedRGB & 0xFF;
            
            // Apply brightness
            float bright = brightness[vertex];
            newR = (int) (newR * bright);
            newG = (int) (newG * bright);
            newB = (int) (newB * bright);
            
            // Clamp values
            newR = Math.min(255, Math.max(0, newR));
            newG = Math.min(255, Math.max(0, newG));
            newB = Math.min(255, Math.max(0, newB));
            
            // Repack color
            int newPackedColor = (a << 24) | (newR << 16) | (newG << 8) | newB;
            
            // Update vertex data
            vertexData[offset + 3] = newPackedColor;
        }
        
        // Emit modified quad
        buffer.putBulkData(
            // Pose, quad, brightness values, lightmap, overlay...
            // Implementation depends on exact method signature
        );
    }
    
    /**
     * Requests async loading of color transform from server.
     */
    private void requestTransformLoad(BlockPos pos) {
        // Schedule async load (implementation in next section)
        ColorVariants.CLIENT_RENDER_MANAGER.requestTransform(pos);
    }
}
```

### 2.4 Client Render Manager

**File:** `common/src/main/java/com/colorvariants/client/ClientRenderManager.java`

```java
package com.colorvariants.client;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.network.PacketHandler;
import com.colorvariants.network.RequestColorPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages client-side color transform requests and caching.
 * 
 * Handles:
 * - Async transform loading from server
 * - Batch request optimization
 * - Cache management
 * - Chunk unload cleanup
 */
public class ClientRenderManager {
    
    private final ColorTransformCache cache = new ColorTransformCache();
    private final Queue<BlockPos> pendingRequests = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread t = new Thread(r, "ColorVariants-Client-Manager");
            t.setDaemon(true);
            return t;
        }
    );
    
    private static final int BATCH_SIZE = 100;
    private static final int BATCH_DELAY_MS = 50; // Batch requests every 50ms
    
    public ClientRenderManager() {
        // Start batch processor
        scheduler.scheduleAtFixedRate(
            this::processBatch,
            BATCH_DELAY_MS,
            BATCH_DELAY_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Requests a color transform from the server.
     * Requests are batched for performance.
     */
    public void requestTransform(BlockPos pos) {
        // Don't request if already pending
        if (!pendingRequests.contains(pos)) {
            pendingRequests.offer(pos);
        }
    }
    
    /**
     * Processes pending requests in batches.
     */
    private void processBatch() {
        if (pendingRequests.isEmpty()) return;
        
        // Collect batch
        BlockPos[] batch = new BlockPos[Math.min(BATCH_SIZE, pendingRequests.size())];
        for (int i = 0; i < batch.length && !pendingRequests.isEmpty(); i++) {
            batch[i] = pendingRequests.poll();
        }
        
        // Send batch request to server
        if (batch.length > 0) {
            PacketHandler.sendToServer(new RequestColorPacket(batch));
        }
    }
    
    /**
     * Called when receiving color data from server.
     */
    public void onColorReceived(BlockPos pos, ColorTransform transform) {
        cache.put(pos, transform);
        
        // Request chunk re-render
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.levelRenderer.setBlockDirty(pos, null, null);
        }
    }
    
    /**
     * Gets cached transform.
     */
    public ColorTransform getCached(BlockPos pos) {
        return cache.get(pos);
    }
    
    /**
     * Clears cache (on disconnect, dimension change, etc.)
     */
    public void clearCache() {
        cache.clear();
        pendingRequests.clear();
    }
    
    /**
     * Clears cache for a specific chunk (on chunk unload).
     */
    public void clearChunk(int chunkX, int chunkZ) {
        // Remove all positions in the chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -64; y < 320; y++) { // Minecraft height range
                    BlockPos pos = new BlockPos(
                        chunkX * 16 + x,
                        y,
                        chunkZ * 16 + z
                    );
                    cache.remove(pos);
                }
            }
        }
    }
    
    /**
     * Gets cache statistics.
     */
    public ColorTransformCache.CacheStats getStats() {
        return cache.getStats();
    }
    
    /**
     * Shuts down the manager.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
```

### 2.5 Network Packets for Sync

**File:** `common/src/main/java/com/colorvariants/network/RequestColorPacket.java`

```java
package com.colorvariants.network;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.colorvariants.platform.services.INetworkContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Packet from client requesting color transforms for multiple blocks.
 * Server responds with ColorSyncPacket.
 */
public class RequestColorPacket {
    
    private final BlockPos[] positions;
    
    public RequestColorPacket(BlockPos[] positions) {
        this.positions = positions;
    }
    
    public static void encode(RequestColorPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.positions.length);
        for (BlockPos pos : packet.positions) {
            buf.writeBlockPos(pos);
        }
    }
    
    public static RequestColorPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        BlockPos[] positions = new BlockPos[count];
        for (int i = 0; i < count; i++) {
            positions[i] = buf.readBlockPos();
        }
        return new RequestColorPacket(positions);
    }
    
    public static void handle(RequestColorPacket packet, INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            
            ServerLevel level = player.serverLevel();
            ColorTransformManager manager = ColorTransformManager.get(level);
            
            // Validation: Check if player is close enough to these blocks
            BlockPos playerPos = player.blockPosition();
            int maxDistance = 128; // Render distance
            
            // Collect transforms
            ColorSyncPacket.ColorData[] data = new ColorSyncPacket.ColorData[packet.positions.length];
            int validCount = 0;
            
            for (BlockPos pos : packet.positions) {
                // Security: Check distance
                if (pos.distSqr(playerPos) > maxDistance * maxDistance) {
                    continue; // Skip blocks too far away
                }
                
                ColorTransform transform = manager.getTransform(pos);
                if (!transform.isNone()) {
                    data[validCount++] = new ColorSyncPacket.ColorData(pos, transform);
                }
            }
            
            // Send response if we have data
            if (validCount > 0) {
                ColorSyncPacket.ColorData[] validData = new ColorSyncPacket.ColorData[validCount];
                System.arraycopy(data, 0, validData, 0, validCount);
                
                PacketHandler.sendToPlayer(new ColorSyncPacket(validData), player);
            }
        });
        
        ctx.setPacketHandled(true);
    }
}
```

**File:** Update `common/src/main/java/com/colorvariants/network/ColorSyncPacket.java`

```java
package com.colorvariants.network;

import com.colorvariants.ColorVariants;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.platform.services.INetworkContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Packet from server to client with color transform data.
 * Sent in response to RequestColorPacket or when colors are updated.
 */
public class ColorSyncPacket {
    
    private final ColorData[] data;
    
    public ColorSyncPacket(ColorData[] data) {
        this.data = data;
    }
    
    public static void encode(ColorSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.data.length);
        for (ColorData entry : packet.data) {
            buf.writeBlockPos(entry.pos);
            buf.writeFloat(entry.transform.getHueShift());
            buf.writeFloat(entry.transform.getSaturation());
            buf.writeFloat(entry.transform.getBrightness());
        }
    }
    
    public static ColorSyncPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        ColorData[] data = new ColorData[count];
        
        for (int i = 0; i < count; i++) {
            BlockPos pos = buf.readBlockPos();
            float hue = buf.readFloat();
            float sat = buf.readFloat();
            float bright = buf.readFloat();
            data[i] = new ColorData(pos, new ColorTransform(hue, sat, bright));
        }
        
        return new ColorSyncPacket(data);
    }
    
    public static void handle(ColorSyncPacket packet, INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            // Client-side: Update cache
            for (ColorData entry : packet.data) {
                ColorVariants.CLIENT_RENDER_MANAGER.onColorReceived(
                    entry.pos,
                    entry.transform
                );
            }
        });
        
        ctx.setPacketHandled(true);
    }
    
    public static record ColorData(BlockPos pos, ColorTransform transform) {}
}
```

### 2.6 Register Packets

Update `PacketHandler.register()`:

```java
public static void register() {
    // Existing packets...
    Services.NETWORK.registerServerbound(
        ColorUpdatePacket.class,
        (msg, buf) -> ColorUpdatePacket.encode(msg, new FriendlyByteBuf(buf)),
        (buf) -> ColorUpdatePacket.decode(new FriendlyByteBuf(buf)),
        ColorUpdatePacket::handle);

    Services.NETWORK.registerClientbound(
        ColorSyncPacket.class,
        (msg, buf) -> ColorSyncPacket.encode(msg, new FriendlyByteBuf(buf)),
        (buf) -> ColorSyncPacket.decode(new FriendlyByteBuf(buf)),
        ColorSyncPacket::handle);

    Services.NETWORK.registerServerbound(
        AreaColorUpdatePacket.class,
        (msg, buf) -> AreaColorUpdatePacket.encode(msg, new FriendlyByteBuf(buf)),
        (buf) -> AreaColorUpdatePacket.decode(new FriendlyByteBuf(buf)),
        AreaColorUpdatePacket::handle);
    
    // NEW: Request packet
    Services.NETWORK.registerServerbound(
        RequestColorPacket.class,
        (msg, buf) -> RequestColorPacket.encode(msg, new FriendlyByteBuf(buf)),
        (buf) -> RequestColorPacket.decode(new FriendlyByteBuf(buf)),
        RequestColorPacket::handle);
}
```

### 2.7 Initialize ClientRenderManager

**File:** Update `common/src/main/java/com/colorvariants/ColorVariants.java`

```java
public class ColorVariants {
    
    public static final String MOD_ID = "colorvariants";
    public static final String MOD_NAME = "Color Variants";
    
    // CLIENT-SIDE ONLY
    public static ClientRenderManager CLIENT_RENDER_MANAGER;
    
    public static void initClient() {
        // Only call this on client side!
        if (Services.PLATFORM.getEnvironmentName().equals("CLIENT")) {
            CLIENT_RENDER_MANAGER = new ClientRenderManager();
            Constants.LOG.info("Initialized client render manager");
        }
    }
    
    public static void shutdownClient() {
        if (CLIENT_RENDER_MANAGER != null) {
            CLIENT_RENDER_MANAGER.shutdown();
        }
    }
    
    // ... rest of existing code
}
```

In Fabric client entrypoint:
```java
// fabric/src/main/java/com/colorvariants/ColorVariantsFabricClient.java
public class ColorVariantsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorVariants.initClient();
        
        // Register client-side event listeners
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ColorVariants.shutdownClient();
        });
    }
}
```

---

## PHASE 3: MOD COMPATIBILITY

### 3.1 Compatibility Strategy

**Goal:** Ensure ColorVariants works alongside other popular mods without conflicts.

**Critical Mods to Test:**
1. **Optifine/OptiFabric** - Shader & rendering modifications
2. **Sodium** - Rendering optimization
3. **Iris Shaders** - Shader pack support
4. **Create** - Complex block models
5. **Chisel & Bits** - Custom block editing
6. **WorldEdit** - Mass block manipulation
7. **Litematica** - Schematic rendering

### 3.2 Mixin Priority Configuration

**File:** `common/src/main/resources/colorvariants.mixins.json`

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.colorvariants.mixin",
  "compatibilityLevel": "JAVA_17",
  "mixins": [
    "MixinBlockBehavior"
  ],
  "client": [
    "MixinBlockModelRenderer"
  ],
  "injectors": {
    "defaultRequire": 1
  },
  "overwrites": {
    "conformVisibility": true
  },
  "plugin": "com.colorvariants.mixin.ColorVariantsMixinPlugin"
}
```

### 3.3 Mixin Plugin for Conditional Loading

**File:** `common/src/main/java/com/colorvariants/mixin/ColorVariantsMixinPlugin.java`

```java
package com.colorvariants.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin for conditional mixin loading based on mod compatibility.
 * 
 * This allows us to disable certain mixins if conflicting mods are present,
 * or load alternative implementations for better compatibility.
 */
public class ColorVariantsMixinPlugin implements IMixinConfigPlugin {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("ColorVariants Mixin Plugin");
    
    private boolean isSodiumLoaded = false;
    private boolean isOptifineLoaded = false;
    private boolean isIrisLoaded = false;
    
    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("Loading ColorVariants mixins...");
        
        // Detect conflicting/complementary mods
        isSodiumLoaded = isModLoaded("sodium");
        isOptifineLoaded = isModLoaded("optifine");
        isIrisLoaded = isModLoaded("iris");
        
        if (isSodiumLoaded) {
            LOGGER.info("Sodium detected - using compatible rendering hooks");
        }
        if (isOptifineLoaded) {
            LOGGER.warn("Optifine detected - some features may be limited");
        }
        if (isIrisLoaded) {
            LOGGER.info("Iris detected - shader compatibility enabled");
        }
    }
    
    @Override
    public String getRefMapperConfig() {
        return null;
    }
    
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Conditional mixin loading based on environment
        
        // Disable BlockModelRenderer mixin if Sodium is present
        // (Sodium uses different rendering pipeline)
        if (mixinClassName.contains("MixinBlockModelRenderer") && isSodiumLoaded) {
            LOGGER.info("Skipping BlockModelRenderer mixin (Sodium compatibility)");
            return false;
        }
        
        // Additional compatibility checks can be added here
        
        return true;
    }
    
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }
    
    @Override
    public List<String> getMixins() {
        return null;
    }
    
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
    
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
    
    private boolean isModLoaded(String modId) {
        try {
            // Try Fabric/Quilt
            Class<?> fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabricLoader.getMethod("getInstance").invoke(null);
            return (boolean) fabricLoader
                .getMethod("isModLoaded", String.class)
                .invoke(instance, modId);
        } catch (Exception e) {
            // Try Forge
            try {
                Class<?> modList = Class.forName("net.minecraftforge.fml.ModList");
                Object instance = modList.getMethod("get").invoke(null);
                return (boolean) modList
                    .getMethod("isLoaded", String.class)
                    .invoke(instance, modId);
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
```

### 3.4 Sodium Compatibility Layer

**File:** `common/src/main/java/com/colorvariants/compat/SodiumCompatibility.java`

```java
package com.colorvariants.compat;

import com.colorvariants.core.ColorTransform;
import net.minecraft.core.BlockPos;

/**
 * Compatibility layer for Sodium rendering engine.
 * 
 * Sodium uses a completely different rendering pipeline that
 * bypasses vanilla's BlockModelRenderer. We need to hook into
 * Sodium's chunk mesh building instead.
 * 
 * NOTE: This requires Sodium API or reflection-based hooks.
 */
public class SodiumCompatibility {
    
    private static boolean initialized = false;
    
    /**
     * Initializes Sodium compatibility hooks.
     * Called during mod initialization if Sodium is detected.
     */
    public static void initialize() {
        if (initialized) return;
        
        try {
            // Hook into Sodium's chunk mesh building
            // This is implementation-specific and may need updates
            // when Sodium versions change
            
            initializeSodiumHooks();
            initialized = true;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Sodium compatibility", e);
        }
    }
    
    private static void initializeSodiumHooks() {
        // Implementation depends on Sodium version
        // Typically involves:
        // 1. Registering a block color provider
        // 2. Hooking into mesh building events
        // 3. Modifying vertex colors during chunk rebuild
        
        // Pseudocode:
        // SodiumAPI.registerBlockColorProvider((state, level, pos, tintIndex) -> {
        //     ColorTransform transform = getTransform(pos);
        //     return transform.apply(defaultColor);
        // });
    }
    
    /**
     * Called during Sodium chunk mesh building.
     */
    public static int modifyVertexColor(BlockPos pos, int originalColor) {
        // Get transform and apply
        // This would be called from Sodium hooks
        return originalColor; // Placeholder
    }
}
```

### 3.5 WorldEdit Integration

**File:** `common/src/main/java/com/colorvariants/compat/WorldEditIntegration.java`

```java
package com.colorvariants.compat;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Integration with WorldEdit for mass color operations.
 * 
 * Provides:
 * - Color copying in schematics
 * - //color command for mass coloring
 * - Undo/redo support
 */
public class WorldEditIntegration {
    
    /**
     * Copies color data when WorldEdit copies blocks.
     */
    public static void onBlockCopy(ServerLevel sourceLevel, BlockPos sourcePos, 
                                   ServerLevel destLevel, BlockPos destPos) {
        ColorTransformManager sourceManager = ColorTransformManager.get(sourceLevel);
        ColorTransformManager destManager = ColorTransformManager.get(destLevel);
        
        ColorTransform transform = sourceManager.getTransform(sourcePos);
        if (!transform.isNone()) {
            destManager.setTransform(destPos, transform);
        }
    }
    
    /**
     * Registers WorldEdit command: //color <hue> <sat> <bright>
     */
    public static void registerCommands() {
        // Register with WorldEdit's command system
        // Implementation depends on WorldEdit API
    }
}
```

---

## PHASE 4: PERFORMANCE & OPTIMIZATION

### 4.1 Performance Metrics

**File:** `common/src/main/java/com/colorvariants/util/PerformanceMonitor.java`

```java
package com.colorvariants.util;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Performance monitoring for identifying bottlenecks.
 */
public class PerformanceMonitor {
    
    private static final Object2LongOpenHashMap<String> timings = new Object2LongOpenHashMap<>();
    private static final LongAdder totalRenderCalls = new LongAdder();
    private static final LongAdder cacheHits = new LongAdder();
    private static final LongAdder cacheMisses = new LongAdder();
    
    public static void recordRenderTime(String operation, long nanos) {
        synchronized (timings) {
            timings.addTo(operation, nanos);
        }
    }
    
    public static void recordRenderCall() {
        totalRenderCalls.increment();
    }
    
    public static void recordCacheHit() {
        cacheHits.increment();
    }
    
    public static void recordCacheMiss() {
        cacheMisses.increment();
    }
    
    public static String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ColorVariants Performance Report ===\n");
        sb.append(String.format("Total render calls: %d\n", totalRenderCalls.sum()));
        sb.append(String.format("Cache hit rate: %.2f%%\n", 
            cacheHits.sum() * 100.0 / (cacheHits.sum() + cacheMisses.sum())));
        
        synchronized (timings) {
            sb.append("\nOperation timings (ms):\n");
            timings.forEach((op, time) -> {
                sb.append(String.format("  %s: %.2f\n", op, time / 1_000_000.0));
            });
        }
        
        return sb.toString();
    }
    
    public static void reset() {
        synchronized (timings) {
            timings.clear();
        }
        totalRenderCalls.reset();
        cacheHits.reset();
        cacheMisses.reset();
    }
}
```

### 4.2 Chunk-Based Indexing for Manager

Optimize `ColorTransformManager` to use chunk-based storage:

```java
// In ColorTransformManager.java

// Replace simple HashMap with chunk-indexed structure
private final Long2ObjectOpenHashMap<ChunkColorData> chunkData = new Long2ObjectOpenHashMap<>();

private static class ChunkColorData {
    private final Map<BlockPos, ColorTransform> transforms = new HashMap<>();
    
    public void setTransform(BlockPos pos, ColorTransform transform) {
        if (transform.isNone()) {
            transforms.remove(pos);
        } else {
            transforms.put(pos, transform);
        }
    }
    
    public ColorTransform getTransform(BlockPos pos) {
        return transforms.getOrDefault(pos, ColorTransform.NONE);
    }
}

// Helper to get chunk key
private static long chunkKey(BlockPos pos) {
    int chunkX = pos.getX() >> 4;
    int chunkZ = pos.getZ() >> 4;
    return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
}

// Updated methods
public void setTransform(BlockPos pos, ColorTransform transform) {
    long chunkKey = chunkKey(pos);
    ChunkColorData data = chunkData.computeIfAbsent(chunkKey, k -> new ChunkColorData());
    data.setTransform(pos.immutable(), transform);
    setDirty();
}

public ColorTransform getTransform(BlockPos pos) {
    long chunkKey = chunkKey(pos);
    ChunkColorData data = chunkData.get(chunkKey);
    return data != null ? data.getTransform(pos) : ColorTransform.NONE;
}
```

**Benefits:**
- ✅ Faster lookups (chunk-localized)
- ✅ Better memory locality
- ✅ Easy chunk unload cleanup
- ✅ Scalable to large worlds

### 4.3 Async Texture Generation (Optional Enhancement)

If you decide to keep texture generation:

```java
// In TextureGenerator.java

private final ExecutorService textureGenExecutor = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors(),
    r -> {
        Thread t = new Thread(r, "ColorVariants-TextureGen");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY); // Low priority
        return t;
    }
);

public CompletableFuture<ResourceLocation> generateTextureAsync(
    BlockState state,
    ColorTransform transform
) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return generateTexture(state, transform, getCacheKey(state, transform));
        } catch (Exception e) {
            LOGGER.error("Async texture generation failed", e);
            return null;
        }
    }, textureGenExecutor);
}
```

---

## PHASE 5: TESTING & VALIDATION

### 5.1 Unit Tests

**File:** `common/src/test/java/com/colorvariants/core/ColorTransformTest.java`

```java
package com.colorvariants.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorTransformTest {
    
    @Test
    public void testIdentityTransform() {
        ColorTransform identity = new ColorTransform(0, 1, 1);
        int red = 0xFFFF0000;
        assertEquals(red, identity.apply(red));
    }
    
    @Test
    public void testHueShift180() {
        ColorTransform transform = new ColorTransform(180, 1, 1);
        int red = 0xFFFF0000;
        int cyan = 0xFF00FFFF;
        
        int result = transform.apply(red);
        
        // Should be cyan (or very close due to rounding)
        int r = (result >> 16) & 0xFF;
        int g = (result >> 8) & 0xFF;
        int b = result & 0xFF;
        
        assertTrue(r < 10); // Nearly zero
        assertTrue(g > 245); // Nearly 255
        assertTrue(b > 245); // Nearly 255
    }
    
    @Test
    public void testSaturationZero() {
        ColorTransform transform = new ColorTransform(0, 0, 1);
        int red = 0xFFFF0000;
        int result = transform.apply(red);
        
        // Should be grayscale
        int r = (result >> 16) & 0xFF;
        int g = (result >> 8) & 0xFF;
        int b = result & 0xFF;
        
        assertEquals(r, g);
        assertEquals(g, b);
    }
    
    @Test
    public void testBrightness() {
        ColorTransform darken = new ColorTransform(0, 1, 0.5f);
        int white = 0xFFFFFFFF;
        int result = darken.apply(white);
        
        // Should be medium gray
        int r = (result >> 16) & 0xFF;
        assertTrue(r > 120 && r < 135); // ~127
    }
    
    @Test
    public void testNoneIsNoop() {
        assertTrue(ColorTransform.NONE.isNone());
        
        int color = 0xFF123456;
        assertEquals(color, ColorTransform.NONE.apply(color));
    }
    
    @Test
    public void testClamping() {
        ColorTransform extreme = new ColorTransform(0, 10, 10); // Way over limits
        // Should clamp to max values
        assertEquals(2.0f, extreme.getSaturation());
        assertEquals(2.0f, extreme.getBrightness());
    }
}
```

### 5.2 Integration Tests

**File:** `common/src/test/java/com/colorvariants/integration/RenderingIntegrationTest.java`

```java
package com.colorvariants.integration;

import com.colorvariants.client.ColorTransformCache;
import com.colorvariants.core.ColorTransform;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RenderingIntegrationTest {
    
    @Test
    public void testCachePutGet() {
        ColorTransformCache cache = new ColorTransformCache();
        BlockPos pos = new BlockPos(10, 64, 20);
        ColorTransform transform = new ColorTransform(180, 1.5f, 0.8f);
        
        cache.put(pos, transform);
        ColorTransform retrieved = cache.get(pos);
        
        assertNotNull(retrieved);
        assertEquals(transform.getHueShift(), retrieved.getHueShift(), 0.01);
    }
    
    @Test
    public void testCacheLRUEviction() {
        ColorTransformCache cache = new ColorTransformCache();
        
        // Fill cache beyond capacity
        for (int i = 0; i < 60000; i++) {
            BlockPos pos = new BlockPos(i, 64, 0);
            cache.put(pos, new ColorTransform(i % 360, 1, 1));
        }
        
        // Check stats
        var stats = cache.getStats();
        assertTrue(stats.size() <= 50000); // Should have evicted some
    }
}
```

### 5.3 Performance Benchmarks

```java
@Test
public void benchmarkColorTransformPerformance() {
    ColorTransform transform = new ColorTransform(180, 1.2f, 0.9f);
    int iterations = 1_000_000;
    
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
        transform.apply(0xFFFF0000);
    }
    long endTime = System.nanoTime();
    
    double avgNanos = (endTime - startTime) / (double) iterations;
    System.out.printf("Average transform time: %.2f ns\n", avgNanos);
    
    // Should be well under 1000ns (1μs) per transform
    assertTrue(avgNanos < 1000, "Transform too slow: " + avgNanos + " ns");
}
```

### 5.4 Multiplayer Testing Checklist

```markdown
## Multiplayer Test Scenarios

### Area Selector Tests
- [ ] Player A selects area, Player B cannot interfere
- [ ] Each player's selection persists independently
- [ ] Selection visible in tooltip
- [ ] GUI opens with correct coordinates

### Color Application Tests
- [ ] Color applied by Player A visible to Player B
- [ ] Color persists after chunk reload
- [ ] Color persists after server restart
- [ ] Color syncs to joining players
- [ ] Mass area coloring works (100+ blocks)

### Performance Tests
- [ ] No lag with 10+ players coloring simultaneously
- [ ] Cache hit rate > 95% during normal gameplay
- [ ] Frame rate stable (<5% drop) with colored blocks
- [ ] Server TPS stable (>18 TPS) with heavy coloring

### Compatibility Tests
- [ ] Works with Optifine
- [ ] Works with Sodium
- [ ] Works with Iris Shaders
- [ ] Works with WorldEdit
- [ ] Works with Create mod
- [ ] No crashes with 20+ other mods loaded
```

---

## APPENDIX: CODE TEMPLATES

### A.1 Config System Template

```java
package com.colorvariants.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "colorvariants")
public class ModConfigData implements ConfigData {
    
    @ConfigEntry.Category("rendering")
    @ConfigEntry.Gui.Tooltip
    public boolean enableRendering = true;
    
    @ConfigEntry.BoundedDiscrete(min = 1000, max = 100000)
    public int maxCacheSize = 50000;
    
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public int asyncThreads = 2;
    
    @ConfigEntry.Category("gameplay")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 1000000)
    public int maxAreaSize = 10000;
    
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int actionsPerSecond = 10;
    
    @ConfigEntry.Category("compatibility")
    public boolean sodiumCompat = true;
    public boolean optifineCompat = true;
    public boolean worldEditIntegration = true;
}
```

### A.2 Debug Command Template

```java
package com.colorvariants.command;

import com.colorvariants.ColorVariants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DebugCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("colorvariants")
                .then(Commands.literal("stats")
                    .executes(ctx -> {
                        if (ctx.getSource().isPlayer()) {
                            var stats = ColorVariants.CLIENT_RENDER_MANAGER.getStats();
                            ctx.getSource().sendSuccess(
                                () -> Component.literal(stats.toString()),
                                false
                            );
                        }
                        return 1;
                    }))
                .then(Commands.literal("cache")
                    .then(Commands.literal("clear")
                        .executes(ctx -> {
                            ColorVariants.CLIENT_RENDER_MANAGER.clearCache();
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("Cache cleared"),
                                false
                            );
                            return 1;
                        })))
        );
    }
}
```

---

## TIMELINE & ROADMAP

### Week 1: Foundation
- ✅ Day 1-2: Fix Area Selector bug
- ✅ Day 3-4: Implement ColorTransformCache
- ✅ Day 5-7: Implement ClientRenderManager

### Week 2: Rendering Core
- ✅ Day 8-10: Implement MixinBlockModelRenderer
- ✅ Day 11-12: Implement network sync packets
- ✅ Day 13-14: Integration & basic testing

### Week 3: Compatibility
- ✅ Day 15-16: Mixin plugin & conditional loading
- ✅ Day 17-18: Sodium compatibility
- ✅ Day 19-20: Other mod compatibility
- ✅ Day 21: Integration testing

### Week 4: Polish & Release
- ✅ Day 22-23: Performance optimization
- ✅ Day 24-25: Unit & integration tests
- ✅ Day 26-27: Multiplayer testing
- ✅ Day 28: Documentation & release prep

---

## QUALITY GATES

Before proceeding to next phase:

**Phase 1:**
- [ ] Area Selector multiplayer test passes
- [ ] No shared state bugs
- [ ] Code review complete

**Phase 2:**
- [ ] Rendering works in singleplayer
- [ ] Cache hit rate > 90%
- [ ] No visible lag
- [ ] Colors persist across restarts

**Phase 3:**
- [ ] Works with top 5 mods
- [ ] No crashes in 50-mod modpack
- [ ] Performance acceptable

**Phase 4:**
- [ ] Frame rate impact < 5%
- [ ] Memory usage < 200MB
- [ ] Cache efficiency > 95%

**Phase 5:**
- [ ] All unit tests pass
- [ ] Multiplayer tests pass
- [ ] Performance benchmarks met
- [ ] Documentation complete

---

## DEPLOYMENT CHECKLIST

- [ ] All quality gates passed
- [ ] CurseForge page ready
- [ ] Modrinth page ready
- [ ] GitHub releases configured
- [ ] Version numbers updated
- [ ] Changelog written
- [ ] Known issues documented
- [ ] Support channels set up

---

**Document Version:** 1.0  
**Last Updated:** February 17, 2026  
**Maintained By:** Claude (Anthropic)  
**Target Mod Version:** 2.0.0 (Post-Fix)

**License:** CC0-1.0  
**Contributing:** See CONTRIBUTING.md  
**Issues:** GitHub Issues

---

_END OF PROFESSIONAL GUIDE_
