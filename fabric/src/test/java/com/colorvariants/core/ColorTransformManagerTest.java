package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testPersistence() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos = new BlockPos(10, 20, 30);
        ColorTransform transform = new ColorTransform(100f, 1.5f, 0.8f);

        manager.setTransform(pos, transform);

        CompoundTag tag = new CompoundTag();
        manager.save(tag);

        ColorTransformManager loaded = ColorTransformManager.load(tag);
        assertEquals(1, loaded.getColoredBlockCount());

        ColorTransform loadedTransform = loaded.getTransform(pos);
        assertEquals(transform, loadedTransform);
    }
}
