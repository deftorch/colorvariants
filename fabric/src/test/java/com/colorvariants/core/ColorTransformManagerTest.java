package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testPersistence_SaveAndLoad() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos1 = new BlockPos(10, 20, 30);
        ColorTransform transform1 = new ColorTransform(120f, 0.5f, 0.8f);

        manager.setTransform(pos1, transform1);
        assertEquals(1, manager.getColoredBlockCount());

        CompoundTag tag = new CompoundTag();
        manager.save(tag);

        ColorTransformManager loadedManager = ColorTransformManager.load(tag);
        assertEquals(1, loadedManager.getColoredBlockCount());

        ColorTransform loadedTransform = loadedManager.getTransform(pos1);
        assertFalse(loadedTransform.isNone());
        assertEquals(120f, loadedTransform.getHueShift(), 0.01);
        assertEquals(0.5f, loadedTransform.getSaturation(), 0.01);
        assertEquals(0.8f, loadedTransform.getBrightness(), 0.01);
    }
}
