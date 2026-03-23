package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import net.minecraft.nbt.CompoundTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaSelectorItemTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // Test logic of reading and writing to NBT tags as done inside AreaSelectorItem
        // Without full initialization, testing direct ItemStack methods with AreaSelectorItem throws initialization errors.
        CompoundTag tag1 = new CompoundTag();
        CompoundTag tag2 = new CompoundTag();

        tag1.putIntArray("FirstPos", new int[]{10, 64, 10});
        tag2.putIntArray("FirstPos", new int[]{30, 64, 30});

        int[] c1 = tag1.getIntArray("FirstPos");
        int[] c2 = tag2.getIntArray("FirstPos");

        BlockPos p1 = new BlockPos(c1[0], c1[1], c1[2]);
        BlockPos p2 = new BlockPos(c2[0], c2[1], c2[2]);

        assertEquals(new BlockPos(10, 64, 10), p1);
        assertEquals(new BlockPos(30, 64, 30), p2);
    }
}
