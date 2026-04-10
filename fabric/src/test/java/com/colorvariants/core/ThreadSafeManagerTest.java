package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSafeManagerTest {

    @BeforeAll
    public static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testColorTransformManagerConcurrentAccess() throws InterruptedException {
        // Create an anonymous subclass to override setDirty so it doesn't crash from being outside a real game context
        ColorTransformManager manager = new ColorTransformManager() {
            @Override
            public void setDirty() {
                // Do nothing
            }
        };

        int threadCount = 10;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    BlockPos pos = new BlockPos(threadId, j, 0);
                    // We need a transform that is not NONE, because NONE removes it.
                    manager.setTransform(pos, new ColorTransform(100, 1, 1));
                    manager.getTransform(pos);
                    if (j % 2 == 0) {
                        manager.removeTransform(pos);
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        // 10 threads * 500 remaining elements = 5000 elements.
        assertEquals(5000, manager.getColoredBlockCount(), "Concurrent access should correctly count block transforms");
    }
}
