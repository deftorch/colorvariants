package com.colorvariants.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;

public class AreaSelectorItemTest {

    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        AreaSelectorItem item = Mockito.mock(AreaSelectorItem.class);
        doCallRealMethod().when(item).setFirstPos(Mockito.any(ItemStack.class), Mockito.any(BlockPos.class));
        doCallRealMethod().when(item).getFirstPos(Mockito.any(ItemStack.class));
        doCallRealMethod().when(item).setSecondPos(Mockito.any(ItemStack.class), Mockito.any(BlockPos.class));
        doCallRealMethod().when(item).getSecondPos(Mockito.any(ItemStack.class));

        ItemStack player1Stack = new ItemStack(Items.STICK);
        ItemStack player2Stack = new ItemStack(Items.STICK);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertTrue(item.getFirstPos(player1Stack).isPresent());
        assertTrue(item.getFirstPos(player2Stack).isPresent());

        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).get());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).get());
    }
}