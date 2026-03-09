package com.colorvariants.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaSelectorItemTest {

    @BeforeAll
    static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // Use Mockito to mock AreaSelectorItem to bypass the constructor issue with Registries
        AreaSelectorItem item = Mockito.mock(AreaSelectorItem.class, Mockito.CALLS_REAL_METHODS);

        // We can use a vanilla item for the ItemStack
        ItemStack player1Stack = new ItemStack(Items.STICK);
        ItemStack player2Stack = new ItemStack(Items.STICK);

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
