package com.colorvariants.core;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UndoRedoManagerTest {

    private ServerLevel mockLevel;
    private DimensionDataStorage mockStorage;
    private ColorTransformManager transformManager;

    @BeforeAll
    public static void setupGlobal() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @BeforeEach
    public void setup() {
        mockLevel = mock(ServerLevel.class);
        mockStorage = mock(DimensionDataStorage.class);
        transformManager = new ColorTransformManager();

        when(mockLevel.getDataStorage()).thenReturn(mockStorage);
        when(mockStorage.computeIfAbsent(any(), any(), anyString())).thenReturn(transformManager);
    }

    @Test
    public void testUndoRedoSingleAction() {
        UndoRedoManager undoRedoManager = new UndoRedoManager();
        BlockPos pos = new BlockPos(1, 2, 3);
        ColorTransform oldTransform = ColorTransform.NONE;
        ColorTransform newTransform = new ColorTransform(50f, 0.5f, 0.5f);

        // Perform action
        undoRedoManager.recordAction(mockLevel, pos, oldTransform, newTransform);
        transformManager.setTransform(pos, newTransform);

        assertTrue(transformManager.hasTransform(pos));
        assertEquals(1, undoRedoManager.getUndoSize());
        assertEquals(0, undoRedoManager.getRedoSize());
        assertTrue(undoRedoManager.canUndo());
        assertFalse(undoRedoManager.canRedo());

        // Undo
        boolean undoResult = undoRedoManager.undo();
        assertTrue(undoResult);
        assertFalse(transformManager.hasTransform(pos));
        assertEquals(0, undoRedoManager.getUndoSize());
        assertEquals(1, undoRedoManager.getRedoSize());

        // Redo
        boolean redoResult = undoRedoManager.redo();
        assertTrue(redoResult);
        assertTrue(transformManager.hasTransform(pos));
        assertEquals(newTransform, transformManager.getTransform(pos));
        assertEquals(1, undoRedoManager.getUndoSize());
        assertEquals(0, undoRedoManager.getRedoSize());
    }

    @Test
    public void testUndoRedoBatchAction() {
        UndoRedoManager undoRedoManager = new UndoRedoManager();
        List<BlockPos> positions = Arrays.asList(
                new BlockPos(1, 1, 1),
                new BlockPos(2, 2, 2)
        );
        ColorTransform transform = new ColorTransform(100f, 1.0f, 1.0f);

        undoRedoManager.recordBatchAction(mockLevel, positions, transform);
        for (BlockPos pos : positions) {
            transformManager.setTransform(pos, transform);
        }

        assertTrue(transformManager.hasTransform(positions.get(0)));
        assertTrue(transformManager.hasTransform(positions.get(1)));

        undoRedoManager.undo();

        assertFalse(transformManager.hasTransform(positions.get(0)));
        assertFalse(transformManager.hasTransform(positions.get(1)));

        undoRedoManager.redo();

        assertTrue(transformManager.hasTransform(positions.get(0)));
        assertTrue(transformManager.hasTransform(positions.get(1)));
    }

    @Test
    public void testMaxHistorySize() {
        UndoRedoManager undoRedoManager = new UndoRedoManager();
        BlockPos pos = new BlockPos(0, 0, 0);

        for (int i = 0; i < 60; i++) {
            undoRedoManager.recordAction(mockLevel, pos, ColorTransform.NONE, new ColorTransform(i, 1f, 1f));
        }

        assertEquals(50, undoRedoManager.getUndoSize(), "Undo stack should be limited to MAX_HISTORY_SIZE (50)");
    }

    @Test
    public void testClear() {
        UndoRedoManager undoRedoManager = new UndoRedoManager();
        undoRedoManager.recordAction(mockLevel, new BlockPos(0,0,0), ColorTransform.NONE, new ColorTransform(10f, 1f, 1f));
        undoRedoManager.clear();

        assertEquals(0, undoRedoManager.getUndoSize());
        assertFalse(undoRedoManager.canUndo());
    }
}
