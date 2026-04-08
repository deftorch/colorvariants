package com.colorvariants.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSafetyTest {

    @Test
    public void testUndoRedoManager_concurrentAccess() throws InterruptedException {
        UndoRedoManager manager = new UndoRedoManager();
        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        manager.clear();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(0, manager.getUndoSize());
        assertEquals(0, manager.getRedoSize());
    }
}
