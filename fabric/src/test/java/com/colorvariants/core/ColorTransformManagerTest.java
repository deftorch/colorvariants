package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testColorTransformManager_ConcurrentAccess() throws InterruptedException {
        ColorTransformManager manager = new ColorTransformManager();

        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                startLatch.await();
                for (int i = 0; i < operationsPerThread; i++) {
                    BlockPos pos = new BlockPos(i, i, i);
                    manager.setTransform(pos, new ColorTransform(1.0f, 1.0f, 1.0f));
                    manager.getTransform(pos);
                    if (i % 2 == 0) {
                        manager.removeTransform(pos);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            executor.submit(task);
        }

        startLatch.countDown();

        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertDoesNotThrow(() -> {
            if (!completed) throw new RuntimeException("Test timed out");
            manager.getAllTransforms();
        });
    }
}
