package com.colorvariants.platform;

import com.colorvariants.platform.services.IRegistryHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> itemSupplier) {
        T item = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("colorvariants", name),
                itemSupplier.get());
        return () -> item;
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> blockSupplier) {
        T block = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("colorvariants", name),
                blockSupplier.get());
        return () -> block;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name,
            java.util.function.BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> factory) {
        BlockEntityType<T> blockEntity = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                new ResourceLocation("colorvariants", name),
                net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder
                        .create(factory::apply).build());
        return () -> blockEntity;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tabSupplier) {
        // For Fabric, we usually register tabs differently or using FabricItemGroup,
        // but direct registration works too in 1.20+
        CreativeModeTab tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                new ResourceLocation("colorvariants", name), tabSupplier.get());
        return () -> tab;
    }
}
