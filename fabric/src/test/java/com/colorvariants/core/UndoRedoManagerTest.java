package com.colorvariants.core;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class UndoRedoManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void manager_handlesConcurrentModifications() throws InterruptedException {
        UndoRedoManager manager = new UndoRedoManager();
        ServerLevel mockLevel = mock(ServerLevel.class);
        DimensionDataStorage mockStorage = mock(DimensionDataStorage.class);
        when(mockLevel.getDataStorage()).thenReturn(mockStorage);
        when(mockStorage.computeIfAbsent(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(new ColorTransformManager());

        int numThreads = 10;
        int modificationsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < modificationsPerThread; j++) {
                    BlockPos pos = new BlockPos(threadId, j, 0);
                    manager.recordAction(mockLevel, pos, ColorTransform.NONE, new ColorTransform(100f, 1f, 1f));
                }
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Concurrent modifications did not complete in time");
        // Because of the MAX_HISTORY_SIZE limit (50), the undo size should be exactly 50 if numThreads * modificationsPerThread >= 50
        assertEquals(Math.min(50, numThreads * modificationsPerThread), manager.getUndoSize(), "Count should match number of modifications or max history size");
        executor.shutdown();
    }

    @Test
    public void manager_handlesBatchActionProperly() {
        UndoRedoManager manager = new UndoRedoManager();
        ServerLevel mockLevel = mock(ServerLevel.class);
        DimensionDataStorage mockStorage = mock(DimensionDataStorage.class);
        when(mockLevel.getDataStorage()).thenReturn(mockStorage);
        ColorTransformManager colorTransformManager = new ColorTransformManager();
        when(mockStorage.computeIfAbsent(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(colorTransformManager);

        List<BlockPos> positions = new ArrayList<>();
        positions.add(new BlockPos(1, 1, 1));
        positions.add(new BlockPos(2, 2, 2));

        manager.recordBatchAction(mockLevel, positions, new ColorTransform(50f, 0.5f, 0.5f));
        assertEquals(1, manager.getUndoSize());

        manager.undo();
        assertEquals(0, manager.getUndoSize());
        assertEquals(1, manager.getRedoSize());

        manager.redo();
        assertEquals(1, manager.getUndoSize());
        assertEquals(0, manager.getRedoSize());
    }
}
