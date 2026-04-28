package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // We bypass the Item constructor entirely by using an existing item (like Items.APPLE)
        // for the ItemStack, but calling our AreaSelectorItem logic via a mocked/partially mocked instance.
        // Actually, since AreaSelectorItem is just an Item, and it doesn't use the item reference
        // in its static-like NBT methods, we can just use any registered item for the ItemStack.

        ItemStack player1Stack = new ItemStack(Items.STICK);
        ItemStack player2Stack = new ItemStack(Items.STICK);

        // Create an uninitialized mock of AreaSelectorItem to call the methods
        AreaSelectorItem item = Mockito.mock(AreaSelectorItem.class);
        Mockito.doCallRealMethod().when(item).setFirstPos(Mockito.any(ItemStack.class), Mockito.any(BlockPos.class));
        Mockito.doCallRealMethod().when(item).getFirstPos(Mockito.any(ItemStack.class));

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertTrue(item.getFirstPos(player1Stack).isPresent());
        assertTrue(item.getFirstPos(player2Stack).isPresent());
        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).get());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).get());
    }
}
