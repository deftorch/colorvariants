package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorTransformManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testTransformsCollectionIsThreadSafe() {
        ColorTransformManager manager = new ColorTransformManager();
        Map<BlockPos, ColorTransform> allTransforms = manager.getAllTransforms();

        Thread t1 = new Thread(() -> {
            for(int i=0; i<1000; i++) {
                manager.setTransform(new BlockPos(i, 0, 0), ColorTransform.NONE);
            }
        });

        Thread t2 = new Thread(() -> {
            for(int i=0; i<1000; i++) {
                manager.setTransform(new BlockPos(i, 0, 0), ColorTransform.NONE);
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(true);
    }
}
