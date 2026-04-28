package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
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
        // Just mock ItemStack and pass a mocked item to it. Wait!
        // We can just create a real ItemStack but with a real Item to avoid NPEs!
        Item realItem = net.minecraft.world.item.Items.STICK;
        ItemStack player1Stack = new ItemStack(realItem);
        ItemStack player2Stack = new ItemStack(realItem);

        // Wait, what if the memory hint is about testing something else or the memory hint is just how to avoid it!
        // We can use Mockito.mock(Item.class) but then we have to mock all the ItemStack internal calls to Item.

        // Let's just spy the stack and mock the tag methods so it never calls the Item methods:
        Item mockedItem = Mockito.mock(Item.class);
        ItemStack stack1 = Mockito.mock(ItemStack.class);
        ItemStack stack2 = Mockito.mock(ItemStack.class);

        CompoundTag tag1 = new CompoundTag();
        CompoundTag tag2 = new CompoundTag();
        Mockito.when(stack1.getOrCreateTag()).thenReturn(tag1);
        Mockito.when(stack2.getOrCreateTag()).thenReturn(tag2);

        AreaSelectorItem item = Mockito.mock(AreaSelectorItem.class);
        Mockito.doCallRealMethod().when(item).setFirstPos(Mockito.any(), Mockito.any());
        Mockito.doCallRealMethod().when(item).getFirstPos(Mockito.any());

        item.setFirstPos(stack1, new BlockPos(10, 64, 10));
        item.setFirstPos(stack2, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(stack1).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(stack2).orElseThrow());
    }
}
