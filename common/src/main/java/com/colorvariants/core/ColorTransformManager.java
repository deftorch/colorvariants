package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages color transformations for blocks in a world.
 * Handles persistence and synchronization.
 */
public class ColorTransformManager extends SavedData {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorTransformManager.class);
    private static final String DATA_NAME = "colorvariants_transforms";
    
    private final Map<BlockPos, ColorTransform> transforms = new HashMap<>();
    
    public ColorTransformManager() {
    }
    
    /**
     * Gets the ColorTransformManager for a world.
     */
    public static ColorTransformManager get(Level world) {
        if (!(world instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("Can only get manager on server side");
        }
        
        return serverLevel.getDataStorage().computeIfAbsent(
            ColorTransformManager::load,
            ColorTransformManager::new,
            DATA_NAME
        );
    }
    
    /**
     * Sets a color transform for a block.
     */
    public void setTransform(BlockPos pos, ColorTransform transform) {
        if (transform == null || transform.isNone()) {
            transforms.remove(pos.immutable());
        } else {
            transforms.put(pos.immutable(), transform);
        }
        
        setDirty();
    }
    
    /**
     * Gets the color transform for a block.
     */
    public ColorTransform getTransform(BlockPos pos) {
        return transforms.getOrDefault(pos, ColorTransform.NONE);
    }
    
    /**
     * Removes the color transform for a block.
     */
    public void removeTransform(BlockPos pos) {
        if (transforms.remove(pos) != null) {
            setDirty();
        }
    }
    
    /**
     * Checks if a block has a color transform.
     */
    public boolean hasTransform(BlockPos pos) {
        return transforms.containsKey(pos);
    }
    
    /**
     * Gets all transforms in the manager.
     */
    public Map<BlockPos, ColorTransform> getAllTransforms() {
        return new HashMap<>(transforms);
    }
    
    /**
     * Clears all transforms.
     */
    public void clearAll() {
        transforms.clear();
        setDirty();
    }
    
    /**
     * Gets the number of colored blocks.
     */
    public int getColoredBlockCount() {
        return transforms.size();
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        
        for (Map.Entry<BlockPos, ColorTransform> entry : transforms.entrySet()) {
            CompoundTag blockTag = new CompoundTag();
            
            BlockPos pos = entry.getKey();
            blockTag.putInt("x", pos.getX());
            blockTag.putInt("y", pos.getY());
            blockTag.putInt("z", pos.getZ());
            
            blockTag.put("transform", entry.getValue().save());
            
            list.add(blockTag);
        }
        
        tag.put("Transforms", list);
        tag.putInt("Count", transforms.size());
        
        LOGGER.debug("Saved {} colored blocks", transforms.size());
        
        return tag;
    }
    
    /**
     * Loads data from NBT.
     */
    public static ColorTransformManager load(CompoundTag tag) {
        ColorTransformManager manager = new ColorTransformManager();
        
        if (tag.contains("Transforms", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Transforms", Tag.TAG_COMPOUND);
            
            for (int i = 0; i < list.size(); i++) {
                CompoundTag blockTag = list.getCompound(i);
                
                int x = blockTag.getInt("x");
                int y = blockTag.getInt("y");
                int z = blockTag.getInt("z");
                BlockPos pos = new BlockPos(x, y, z);
                
                CompoundTag transformTag = blockTag.getCompound("transform");
                ColorTransform transform = ColorTransform.load(transformTag);
                
                manager.transforms.put(pos, transform);
            }
            
            LOGGER.info("Loaded {} colored blocks", manager.transforms.size());
        }
        
        return manager;
    }
    
    /**
     * Gets statistics about the manager.
     */
    public String getStats() {
        return String.format("ColorTransformManager: %d colored blocks", 
            transforms.size());
    }
}
