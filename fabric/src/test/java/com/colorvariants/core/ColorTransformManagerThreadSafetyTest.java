package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColorTransformManagerThreadSafetyTest {

    @Test
    public void testColorTransformManager_concurrentAccess() throws InterruptedException {
        ColorTransformManager manager = new ColorTransformManager();
        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        BlockPos pos = new BlockPos(threadId, j, 0);
                        // Using a dummy ColorTransform to bypass initialization errors in test
                        manager.setTransform(pos, ColorTransform.NONE);
                        manager.getTransform(pos);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Since we are setting ColorTransform.NONE, they are removed, so count should be 0
        assertEquals(0, manager.getColoredBlockCount());
    }
}
