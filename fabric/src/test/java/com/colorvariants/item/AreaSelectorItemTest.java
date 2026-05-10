package com.colorvariants.item;

import com.colorvariants.ColorVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // We mock the Item registry problem by mocking AreaSelectorItem and calling real methods
        AreaSelectorItem item = Mockito.mock(AreaSelectorItem.class, Mockito.CALLS_REAL_METHODS);
        // We must mock the ItemStack to avoid NPE in uninitialized registry
        ItemStack player1Stack = Mockito.mock(ItemStack.class);
        ItemStack player2Stack = Mockito.mock(ItemStack.class);

        // Setup tag mocks
        net.minecraft.nbt.CompoundTag tag1 = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.CompoundTag tag2 = new net.minecraft.nbt.CompoundTag();
        Mockito.when(player1Stack.getOrCreateTag()).thenReturn(tag1);
        Mockito.when(player2Stack.getOrCreateTag()).thenReturn(tag2);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).orElseThrow());
    }
}
