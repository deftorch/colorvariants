package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.mockito.Mockito;

class AreaSelectorItemTest {

    private AreaSelectorItem item;

    @BeforeAll
    static void initMinecraft() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() {
        // Mock the area selector item class to bypass constructor errors.
        item = Mockito.mock(AreaSelectorItem.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // Use a standard item from the registry that is guaranteed to exist.
        ItemStack player1Stack = new ItemStack(Items.STICK);
        ItemStack player2Stack = new ItemStack(Items.STICK);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).orElseThrow());
        assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).orElseThrow());
    }
}
