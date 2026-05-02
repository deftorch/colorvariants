package com.colorvariants.core;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ColorTransformManagerTest {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void testSetAndGetTransform() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos = new BlockPos(1, 2, 3);
        ColorTransform transform = new ColorTransform(100f, 0.5f, 0.8f);

        manager.setTransform(pos, transform);

        assertEquals(transform, manager.getTransform(pos));
        assertTrue(manager.hasTransform(pos));
    }
}
