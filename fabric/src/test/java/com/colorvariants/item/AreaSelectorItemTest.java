package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AreaSelectorItemTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    public void areaSelector_twoPlayersHaveIndependentPositions() {
        // We bypass the actual item constructor to avoid exceptions
        AreaSelectorItem item = mock(AreaSelectorItem.class);
        when(item.getFirstPos(org.mockito.ArgumentMatchers.any())).thenCallRealMethod();
        when(item.getSecondPos(org.mockito.ArgumentMatchers.any())).thenCallRealMethod();
        org.mockito.Mockito.doCallRealMethod().when(item).setFirstPos(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.doCallRealMethod().when(item).setSecondPos(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        ItemStack player1Stack = mock(ItemStack.class);
        ItemStack player2Stack = mock(ItemStack.class);

        CompoundTag tag1 = new CompoundTag();
        CompoundTag tag2 = new CompoundTag();

        when(player1Stack.getOrCreateTag()).thenReturn(tag1);
        when(player2Stack.getOrCreateTag()).thenReturn(tag2);

        BlockPos pos1 = new BlockPos(10, 64, 10);
        BlockPos pos2 = new BlockPos(30, 64, 30);

        item.setFirstPos(player1Stack, pos1);
        item.setFirstPos(player2Stack, pos2);

        Optional<BlockPos> retrievedPos1 = item.getFirstPos(player1Stack);
        Optional<BlockPos> retrievedPos2 = item.getFirstPos(player2Stack);

        assertTrue(retrievedPos1.isPresent(), "Player 1 stack should have first pos");
        assertTrue(retrievedPos2.isPresent(), "Player 2 stack should have first pos");
        assertEquals(pos1, retrievedPos1.get(), "Player 1 first position should match");
        assertEquals(pos2, retrievedPos2.get(), "Player 2 first position should match");
    }
}
