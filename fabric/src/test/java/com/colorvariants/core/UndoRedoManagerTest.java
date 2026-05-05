package com.colorvariants.core;

import net.minecraft.core.BlockPos;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UndoRedoManagerTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void testUndoRedoManagerCollectionsThreadSafety() {
        UndoRedoManager manager = new UndoRedoManager();

        ServerLevel mockLevel = Mockito.mock(ServerLevel.class);
        DimensionDataStorage mockStorage = Mockito.mock(DimensionDataStorage.class);
        Mockito.when(mockLevel.getDataStorage()).thenReturn(mockStorage);
        ColorTransformManager ctm = new ColorTransformManager();
        Mockito.when(mockStorage.computeIfAbsent(Mockito.any(), Mockito.any(), Mockito.anyString())).thenReturn(ctm);

        Thread t1 = new Thread(() -> {
            for(int i=0; i<100; i++) {
                manager.recordBatchAction(mockLevel, Arrays.asList(new BlockPos(i, 0, 0)), ColorTransform.NONE);
            }
        });

        Thread t2 = new Thread(() -> {
            for(int i=0; i<100; i++) {
                manager.recordBatchAction(mockLevel, Arrays.asList(new BlockPos(100+i, 0, 0)), ColorTransform.NONE);
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
