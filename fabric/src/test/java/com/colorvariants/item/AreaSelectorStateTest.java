package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AreaSelectorStateTest {

    @BeforeEach
    void setUp() {
        // Reset static state
        AreaSelectorState.reset();
    }

    @Test
    void testIndependentState() {
        // Use real CompoundTags instead of ItemStacks
        // This avoids complex mocking and dependencies
        CompoundTag tag1 = new CompoundTag();
        CompoundTag tag2 = new CompoundTag();

        BlockPos pos1 = new BlockPos(10, 64, 10);
        BlockPos pos2 = new BlockPos(20, 64, 20);

        // Set position on tag 1
        AreaSelectorState.setFirstPos(tag1, pos1);

        // Assert tag 1 has pos1
        assertEquals(pos1, AreaSelectorState.getFirstPos(tag1).orElse(null));

        // Set position on tag 2
        AreaSelectorState.setFirstPos(tag2, pos2);

        // Assert tag 2 has pos2
        assertEquals(pos2, AreaSelectorState.getFirstPos(tag2).orElse(null));

        // CRITICAL: Assert tag 1 still has pos1 (this will fail with static fields)
        assertEquals(pos1, AreaSelectorState.getFirstPos(tag1).orElse(null),
            "Tag 1 should retain its own position, but it was overwritten by Tag 2");
    }
}
