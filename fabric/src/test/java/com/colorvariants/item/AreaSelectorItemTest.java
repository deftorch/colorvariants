package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import net.minecraft.world.flag.FeatureFlagSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // Need to use Mockito to mock AreaSelectorItem
        AreaSelectorItem itemLogic = Mockito.mock(AreaSelectorItem.class, Mockito.CALLS_REAL_METHODS);
        Mockito.when(itemLogic.requiredFeatures()).thenReturn(FeatureFlagSet.of());

        ItemStack player1Stack = new ItemStack(itemLogic);
        ItemStack player2Stack = new ItemStack(itemLogic);

        itemLogic.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        itemLogic.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), itemLogic.getFirstPos(player1Stack).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), itemLogic.getFirstPos(player2Stack).orElseThrow());
    }
}
