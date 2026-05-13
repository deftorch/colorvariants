package com.colorvariants.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.Mockito;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        Item mockItem = Mockito.mock(AreaSelectorItem.class, Mockito.CALLS_REAL_METHODS);
        ItemStack player1Stack = new ItemStack(mockItem);
        ItemStack player2Stack = new ItemStack(mockItem);
        AreaSelectorItem item = (AreaSelectorItem) mockItem;

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertTrue(item.getFirstPos(player1Stack).isPresent());
        assertTrue(item.getFirstPos(player2Stack).isPresent());
        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).get());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).get());
    }
}
