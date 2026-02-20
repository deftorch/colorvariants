package com.colorvariants.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

/**
 * Handles state logic for Area Selector.
 * Refactored out of AreaSelectorItem to allow testing and fix shared state bug.
 */
public class AreaSelectorState {

    private static final String NBT_FIRST_POS = "FirstPos";
    private static final String NBT_SECOND_POS = "SecondPos";

    public static Optional<BlockPos> getFirstPos(CompoundTag tag) {
        if (tag == null || !tag.contains(NBT_FIRST_POS)) return Optional.empty();
        int[] coords = tag.getIntArray(NBT_FIRST_POS);
        if (coords.length != 3) return Optional.empty();
        return Optional.of(new BlockPos(coords[0], coords[1], coords[2]));
    }

    public static void setFirstPos(CompoundTag tag, BlockPos pos) {
        if (tag == null) return;
        tag.putIntArray(NBT_FIRST_POS, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    public static void removeFirstPos(CompoundTag tag) {
        if (tag != null) tag.remove(NBT_FIRST_POS);
    }

    public static Optional<BlockPos> getSecondPos(CompoundTag tag) {
        if (tag == null || !tag.contains(NBT_SECOND_POS)) return Optional.empty();
        int[] coords = tag.getIntArray(NBT_SECOND_POS);
        if (coords.length != 3) return Optional.empty();
        return Optional.of(new BlockPos(coords[0], coords[1], coords[2]));
    }

    public static void setSecondPos(CompoundTag tag, BlockPos pos) {
        if (tag == null) return;
        tag.putIntArray(NBT_SECOND_POS, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    public static void removeSecondPos(CompoundTag tag) {
        if (tag != null) tag.remove(NBT_SECOND_POS);
    }

    public static void reset() {
        // No global state to reset anymore.
        // Individual item state is cleared by modifying the item stack NBT if needed.
    }
}
