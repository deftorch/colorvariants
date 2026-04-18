package com.colorvariants.core;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testSetAndGetTransform() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos = new BlockPos(10, 20, 30);
        ColorTransform transform = new ColorTransform(100f, 0.5f, 0.8f);

        manager.setTransform(pos, transform);

        ColorTransform retrieved = manager.getTransform(pos);
        assertNotNull(retrieved);
        assertEquals(100f, retrieved.getHueShift());
        assertEquals(0.5f, retrieved.getSaturation());
        assertEquals(0.8f, retrieved.getBrightness());
    }

    @Test
    public void testRemoveTransform() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos = new BlockPos(10, 20, 30);
        ColorTransform transform = new ColorTransform(100f, 0.5f, 0.8f);

        manager.setTransform(pos, transform);
        assertTrue(manager.hasTransform(pos));

        manager.removeTransform(pos);
        assertFalse(manager.hasTransform(pos));
        assertTrue(manager.getTransform(pos).isNone());
    }

    @Test
    public void testGetAllTransforms() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos1 = new BlockPos(10, 20, 30);
        BlockPos pos2 = new BlockPos(40, 50, 60);

        manager.setTransform(pos1, new ColorTransform(10f, 0.1f, 0.2f));
        manager.setTransform(pos2, new ColorTransform(20f, 0.3f, 0.4f));

        Map<BlockPos, ColorTransform> allTransforms = manager.getAllTransforms();
        assertEquals(2, allTransforms.size());
        assertTrue(allTransforms.containsKey(pos1));
        assertTrue(allTransforms.containsKey(pos2));
    }

    @Test
    public void testClearAll() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos = new BlockPos(10, 20, 30);
        manager.setTransform(pos, new ColorTransform(100f, 0.5f, 0.8f));

        assertEquals(1, manager.getColoredBlockCount());

        manager.clearAll();

        assertEquals(0, manager.getColoredBlockCount());
        assertFalse(manager.hasTransform(pos));
    }
}
