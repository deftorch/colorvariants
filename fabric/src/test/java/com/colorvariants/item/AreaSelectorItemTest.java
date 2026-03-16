package com.colorvariants.item;

import com.colorvariants.ColorVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // Using Mockito to bypass AreaSelectorItem constructor which throws errors without registry
        AreaSelectorItem item = org.mockito.Mockito.mock(AreaSelectorItem.class);
        org.mockito.Mockito.doCallRealMethod().when(item).setFirstPos(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.doCallRealMethod().when(item).getFirstPos(org.mockito.ArgumentMatchers.any());

        ItemStack player1Stack = new ItemStack(net.minecraft.world.item.Items.STONE);
        ItemStack player2Stack = new ItemStack(net.minecraft.world.item.Items.STONE);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).orElseThrow());
    }
}
