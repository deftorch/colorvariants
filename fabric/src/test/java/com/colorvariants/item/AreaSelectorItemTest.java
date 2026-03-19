package com.colorvariants.item;

import com.colorvariants.core.ColorTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // Mock AreaSelectorItem to avoid Item constructor registry requirements
        AreaSelectorItem item = org.mockito.Mockito.mock(AreaSelectorItem.class);

        // We will call the real methods on the mocked object
        org.mockito.Mockito.doCallRealMethod().when(item).setFirstPos(org.mockito.Mockito.any(), org.mockito.Mockito.any());
        org.mockito.Mockito.doCallRealMethod().when(item).getFirstPos(org.mockito.Mockito.any());

        ItemStack player1Stack = new ItemStack(net.minecraft.world.item.Items.STICK);
        ItemStack player2Stack = new ItemStack(net.minecraft.world.item.Items.STICK);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertTrue(item.getFirstPos(player1Stack).isPresent());
        assertTrue(item.getFirstPos(player2Stack).isPresent());
        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).get());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).get());
    }
}