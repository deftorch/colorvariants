package com.colorvariants.core;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        ColorTransformManager manager = new ColorTransformManager();
        int numThreads = 50;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    BlockPos pos = new BlockPos(threadIndex, j, 0);
                    ColorTransform transform = new ColorTransform(100, 1.0f, 1.0f);
                    manager.setTransform(pos, transform);
                }
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(numThreads * operationsPerThread, manager.getColoredBlockCount(), "Count should match concurrent insertions.");
    }
}
