package com.colorvariants.item;

import com.colorvariants.core.ColorTransform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Color Palette item stores a color transform for quick application.
 */
public class ColorPaletteItem extends Item {
    
    public ColorPaletteItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player.isCrouching()) {
            // Clear stored color
            stack.removeTagKey("ColorTransform");
            player.displayClientMessage(
                Component.translatable("item.colorvariants.color_palette.cleared"), 
                true
            );
        }
        
        return InteractionResultHolder.success(stack);
    }
    
    /**
     * Stores a color transform in the palette.
     */
    public static void storeColor(ItemStack stack, ColorTransform transform) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("ColorTransform", transform.save());
    }
    
    /**
     * Gets the stored color transform.
     */
    public static ColorTransform getStoredColor(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("ColorTransform")) {
            return ColorTransform.load(stack.getTag().getCompound("ColorTransform"));
        }
        return ColorTransform.NONE;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ColorTransform transform = getStoredColor(stack);
        
        if (!transform.isNone()) {
            tooltip.add(Component.translatable("item.colorvariants.color_palette.stored")
                .append(String.format(" H:%.0f S:%.2f B:%.2f", 
                    transform.getHueShift(),
                    transform.getSaturation(),
                    transform.getBrightness())));
        } else {
            tooltip.add(Component.translatable("item.colorvariants.color_palette.empty"));
        }
        
        tooltip.add(Component.translatable("item.colorvariants.color_palette.tooltip"));
    }
}
