package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaSelectorItemTest {

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        // Since we are mocking Item or instantiating Item, we need to bypass registry in unit tests.
        // Or simply use Mockito.
        AreaSelectorItem item = org.mockito.Mockito.mock(AreaSelectorItem.class, org.mockito.Mockito.CALLS_REAL_METHODS);

        ItemStack player1Stack = org.mockito.Mockito.mock(ItemStack.class);
        ItemStack player2Stack = org.mockito.Mockito.mock(ItemStack.class);

        net.minecraft.nbt.CompoundTag tag1 = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.CompoundTag tag2 = new net.minecraft.nbt.CompoundTag();

        org.mockito.Mockito.when(player1Stack.getTag()).thenReturn(tag1);
        org.mockito.Mockito.when(player1Stack.getOrCreateTag()).thenReturn(tag1);

        org.mockito.Mockito.when(player2Stack.getTag()).thenReturn(tag2);
        org.mockito.Mockito.when(player2Stack.getOrCreateTag()).thenReturn(tag2);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        Optional<BlockPos> pos1 = item.getFirstPos(player1Stack);
        Optional<BlockPos> pos2 = item.getFirstPos(player2Stack);

        assertTrue(pos1.isPresent());
        assertTrue(pos2.isPresent());

        assertEquals(new BlockPos(10, 64, 10), pos1.get());
        assertEquals(new BlockPos(30, 64, 30), pos2.get());
    }
}
