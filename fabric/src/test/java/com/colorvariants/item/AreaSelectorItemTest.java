package com.colorvariants.item;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

public class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void twoPlayersHaveIndependentPositions() {
        // Mock the Item to avoid constructor side effects (registry access)
        AreaSelectorItem item = mock(AreaSelectorItem.class);

        // Call real methods for the logic we want to test
        doCallRealMethod().when(item).setFirstPos(any(), any());
        doCallRealMethod().when(item).getFirstPos(any());
        doCallRealMethod().when(item).setSecondPos(any(), any());
        doCallRealMethod().when(item).getSecondPos(any());

        // Mock ItemStack to avoid registry/bootstrap issues in constructor
        ItemStack player1Stack = mock(ItemStack.class);
        ItemStack player2Stack = mock(ItemStack.class);

        // Use real CompoundTag for storage logic
        CompoundTag tag1 = new CompoundTag();
        CompoundTag tag2 = new CompoundTag();

        when(player1Stack.getOrCreateTag()).thenReturn(tag1);
        when(player2Stack.getOrCreateTag()).thenReturn(tag2);

        // Initial state
        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        // In a broken implementation (using static fields), setting player2Stack would overwrite player1Stack's value
        // So this assertion should fail if the bug exists
        BlockPos p1Pos = item.getFirstPos(player1Stack).orElseThrow(() -> new AssertionError("Position should be present"));

        assertEquals(new BlockPos(10, 64, 10), p1Pos, "Player 1 position should not be affected by Player 2");
    }
}
