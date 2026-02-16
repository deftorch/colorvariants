package com.colorvariants.item;

import com.colorvariants.client.gui.ColorPickerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The Color Wand item allows players to open the color picker GUI for blocks.
 */
public class ColorWandItem extends Item {

    public ColorWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // Only open GUI on client side
        if (level.isClientSide) {
            openColorPicker(pos, state);
        }

        return InteractionResult.SUCCESS;
    }

    private void openColorPicker(BlockPos pos, BlockState state) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ColorPickerScreen(pos, state));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.colorvariants.color_wand.tooltip.1"));
        tooltip.add(Component.translatable("item.colorvariants.color_wand.tooltip.2"));
    }
}
