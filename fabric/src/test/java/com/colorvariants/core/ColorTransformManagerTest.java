package com.colorvariants.core;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void manager_handlesConcurrentModifications() throws InterruptedException {
        ColorTransformManager manager = new ColorTransformManager();

        int numThreads = 10;
        int modificationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < modificationsPerThread; j++) {
                    BlockPos pos = new BlockPos(threadId, j, 0);
                    manager.setTransform(pos, new ColorTransform(100f, 1f, 1f));
                }
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Concurrent modifications did not complete in time");
        assertEquals(numThreads * modificationsPerThread, manager.getColoredBlockCount(), "Count should match number of modifications");
        executor.shutdown();
    }

    @Test
    public void manager_properlyFetchesAndRemoves() {
        ColorTransformManager manager = new ColorTransformManager();
        BlockPos pos = new BlockPos(1, 2, 3);
        ColorTransform transform = new ColorTransform(50f, 0.5f, 0.5f);

        manager.setTransform(pos, transform);
        assertTrue(manager.hasTransform(pos));
        assertEquals(transform, manager.getTransform(pos));

        manager.removeTransform(pos);
        assertTrue(!manager.hasTransform(pos));
        assertEquals(ColorTransform.NONE, manager.getTransform(pos));
    }
}
