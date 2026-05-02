package com.colorvariants.core;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class UndoRedoManagerTest {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void testRecordActionThreadSafe() {
        UndoRedoManager manager = new UndoRedoManager();
        Level mockLevel = mock(Level.class);
        BlockPos pos = new BlockPos(0, 0, 0);
        ColorTransform oldTransform = ColorTransform.NONE;
        ColorTransform newTransform = new ColorTransform(0.5f, 0.5f, 0.5f);

        manager.recordAction(mockLevel, pos, oldTransform, newTransform);
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
    }
}
