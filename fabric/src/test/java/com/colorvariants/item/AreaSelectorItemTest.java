package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AreaSelectorItemTest {

    @BeforeAll
    public static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        AreaSelectorItem item = Mockito.mock(AreaSelectorItem.class, Mockito.CALLS_REAL_METHODS);

        ItemStack player1Stack = new ItemStack(item);
        ItemStack player2Stack = new ItemStack(item);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).orElseThrow());
    }
}
