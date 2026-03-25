package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AreaSelectorItemTest {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        AreaSelectorItem item = mock(AreaSelectorItem.class);
        doCallRealMethod().when(item).setFirstPos(Mockito.any(), Mockito.any());
        doCallRealMethod().when(item).getFirstPos(Mockito.any());

        ItemStack player1Stack = mock(ItemStack.class);
        CompoundTag tag1 = new CompoundTag();
        when(player1Stack.getOrCreateTag()).thenReturn(tag1);
        when(player1Stack.getTag()).thenReturn(tag1);

        ItemStack player2Stack = mock(ItemStack.class);
        CompoundTag tag2 = new CompoundTag();
        when(player2Stack.getOrCreateTag()).thenReturn(tag2);
        when(player2Stack.getTag()).thenReturn(tag2);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertTrue(item.getFirstPos(player1Stack).isPresent());
        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).get());

        assertTrue(item.getFirstPos(player2Stack).isPresent());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).get());
    }
}
