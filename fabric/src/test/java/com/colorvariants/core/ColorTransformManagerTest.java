package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void colorTransformManager_threadSafety() throws InterruptedException {
        ColorTransformManager manager = new ColorTransformManager();
        int numThreads = 10;
        int numOps = 1000;
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < numOps; i++) {
                        BlockPos pos = new BlockPos(threadId, i, 0);
                        // We must ensure the transform is NOT considered NONE
                        // because if it's NONE, setTransform will REMOVE it
                        // ColorTransform.NONE is (0, 1, 1). So let's use brightness = 0.5f
                        ColorTransform transform = new ColorTransform(i % 360, 1.0f, 0.5f);
                        manager.setTransform(pos, transform);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(numThreads * numOps, manager.getColoredBlockCount());
    }
}
