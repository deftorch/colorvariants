package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AreaSelectorItemTest {

    @Test
    void areaSelector_twoPlayersHaveIndependentPositions() {
        // We cannot instantiate AreaSelectorItem directly easily without full game context or heavy mocking
        // But since we are in fabric module, let's try to simulate basic item behavior if possible,
        // or just use reflection/subclassing if Item requires registry.
        // Actually, Item constructor is simple.

        AreaSelectorItem item = new AreaSelectorItem(new net.minecraft.world.item.Item.Properties());
        ItemStack player1Stack = new ItemStack(item);
        ItemStack player2Stack = new ItemStack(item);

        item.setFirstPos(player1Stack, new BlockPos(10, 64, 10));
        item.setFirstPos(player2Stack, new BlockPos(30, 64, 30));

        // This should fail if static fields are used
        Assertions.assertEquals(new BlockPos(10, 64, 10), item.getFirstPos(player1Stack).orElseThrow(), "Player 1 pos should be preserved");
        Assertions.assertEquals(new BlockPos(30, 64, 30), item.getFirstPos(player2Stack).orElseThrow(), "Player 2 pos should be preserved");

        // Also verify second pos
        item.setSecondPos(player1Stack, new BlockPos(11, 65, 11));
        item.setSecondPos(player2Stack, new BlockPos(31, 65, 31));

        Assertions.assertEquals(new BlockPos(11, 65, 11), item.getSecondPos(player1Stack).orElseThrow(), "Player 1 second pos should be preserved");
        Assertions.assertEquals(new BlockPos(31, 65, 31), item.getSecondPos(player2Stack).orElseThrow(), "Player 2 second pos should be preserved");
    }
}
