package com.colorvariants.platform.services;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface IRegistryHelper {
    <T extends Item> Supplier<T> registerItem(String name, Supplier<T> itemSupplier);

    <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> blockSupplier);

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name,
            java.util.function.BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> factory);

    Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tabSupplier);
}
