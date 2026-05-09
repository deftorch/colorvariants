package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        AreaSelectorItem mockItem = Mockito.mock(AreaSelectorItem.class, Mockito.CALLS_REAL_METHODS);
        ItemStack player1Stack = new ItemStack(mockItem);
        ItemStack player2Stack = new ItemStack(mockItem);

        mockItem.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        mockItem.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), mockItem.getFirstPos(player1Stack).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), mockItem.getFirstPos(player2Stack).orElseThrow());
    }
}
