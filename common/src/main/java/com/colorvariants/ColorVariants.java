package com.colorvariants;

import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.command.ColorCommand;
import com.colorvariants.config.ModConfig;
import com.colorvariants.item.AreaSelectorItem;
import com.colorvariants.item.ColorPaletteItem;
import com.colorvariants.item.ColorWandItem;
import com.colorvariants.item.EyedropperItem;
import com.colorvariants.network.PacketHandler;
import com.colorvariants.platform.Services;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ColorVariants {

        public static final String MOD_ID = "colorvariants";
        public static final String MOD_NAME = "Color Variants";

        // Items
        public static final Supplier<Item> COLOR_WAND = Services.REGISTRY.registerItem("color_wand",
                        () -> new ColorWandItem(new Item.Properties().stacksTo(1)));

        public static final Supplier<Item> COLOR_PALETTE = Services.REGISTRY.registerItem("color_palette",
                        () -> new ColorPaletteItem(new Item.Properties().stacksTo(1)));

        public static final Supplier<Item> AREA_SELECTOR = Services.REGISTRY.registerItem("area_selector",
                        () -> new AreaSelectorItem(new Item.Properties().stacksTo(1)));

        public static final Supplier<Item> EYEDROPPER = Services.REGISTRY.registerItem("eyedropper",
                        () -> new EyedropperItem(new Item.Properties().stacksTo(1)));

        // Block Entities
        public static final Supplier<BlockEntityType<ColoredBlockEntity>> COLORED_BLOCK_ENTITY = Services.REGISTRY
                        .registerBlockEntity("colored_block", ColoredBlockEntity::new);

        // Creative Tab
        public static final Supplier<CreativeModeTab> CREATIVE_TAB = Services.REGISTRY.registerCreativeTab("tab",
                        () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                                        .title(net.minecraft.network.chat.Component
                                                        .translatable("itemGroup.colorvariants"))
                                        .icon(() -> new ItemStack(COLOR_WAND.get()))
                                        .displayItems((params, output) -> {
                                                output.accept(COLOR_WAND.get());
                                                output.accept(COLOR_PALETTE.get());
                                                output.accept(AREA_SELECTOR.get());
                                                output.accept(EYEDROPPER.get());
                                        })
                                        .build());

        public static void init() {
                Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!",
                                Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

                ModConfig.init();
                PacketHandler.register();
        }
}