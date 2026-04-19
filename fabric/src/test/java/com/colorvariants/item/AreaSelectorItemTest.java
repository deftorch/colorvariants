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
        // Since we bootstrapped Minecraft, we can use the vanilla stick or register a mock.
        // Or we can mock the Item and ItemStack if registry throws IllegalStateException: This registry can't create intrusive holders
        // But since we want to test NBT setting which relies on ItemStack's logic, let's use a registered vanilla item
        // cast to AreaSelectorItem... wait, it's not registered.
        // To avoid IllegalStateException: This registry can't create intrusive holders, we won't instantiate AreaSelectorItem via new AreaSelectorItem(new Item.Properties()).
        // We will mock the AreaSelectorItem since the NBT manipulation relies on the ItemStack anyway! Wait, the NBT methods don't use the item fields, they just modify the ItemStack.
        // Wait, the methods are not static: `public void setFirstPos(ItemStack stack, BlockPos pos)`
        // We can just create a spy or mock of AreaSelectorItem that doesn't invoke the super constructor which accesses the registry.
        // Or better yet, we can use Mockito!

        // Wait, what if we use reflection to instantiate it, or just use Mockito.mock() with CALLS_REAL_METHODS?
        AreaSelectorItem areaSelector = org.mockito.Mockito.mock(AreaSelectorItem.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        ItemStack player1Stack = new ItemStack(net.minecraft.world.item.Items.STICK);
        ItemStack player2Stack = new ItemStack(net.minecraft.world.item.Items.STICK);

        areaSelector.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        areaSelector.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), areaSelector.getFirstPos(player1Stack).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), areaSelector.getFirstPos(player2Stack).orElseThrow());
    }
}
