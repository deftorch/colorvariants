package com.colorvariants.item;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.List;

/**
 * Tool for copying colors from colored blocks (eyedropper).
 */
public class EyedropperItem extends Item {

    private static final String COLOR_TAG = "StoredColor";

    public EyedropperItem(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                clearStoredColor(stack);
                player.displayClientMessage(
                        Component.translatable("item.colorvariants.eyedropper.cleared"),
                        true);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        if (!world.isClientSide) {
            sampleColor(world, pos, stack, player);
        }

        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                clearStoredColor(stack);
                player.displayClientMessage(
                        Component.translatable("item.colorvariants.eyedropper.cleared"),
                        true);
            }
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    private void sampleColor(Level world, BlockPos pos, ItemStack stack, Player player) {
        // Ensure we are on server side before accessing manager
        if (world.isClientSide)
            return;

        ColorTransformManager manager = ColorTransformManager.get(world);
        ColorTransform transform = manager.getTransform(pos);

        if (!transform.isNone()) {
            storeColor(stack, transform);
            player.displayClientMessage(
                    Component.translatable("item.colorvariants.eyedropper.sampled",
                            String.format("H:%.0f S:%.2f B:%.2f",
                                    transform.getHueShift(),
                                    transform.getSaturation(),
                                    transform.getBrightness())),
                    true);
        } else {
            player.displayClientMessage(
                    Component.translatable("item.colorvariants.eyedropper.no_color"),
                    true);
        }
    }

    private void storeColor(ItemStack stack, ColorTransform transform) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag colorTag = transform.save();
        tag.put(COLOR_TAG, colorTag);
    }

    private void clearStoredColor(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(COLOR_TAG);
        }
    }

    public static ColorTransform getStoredColor(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(COLOR_TAG)) {
            CompoundTag colorTag = stack.getTag().getCompound(COLOR_TAG);
            return ColorTransform.load(colorTag);
        }
        return ColorTransform.NONE;
    }

    public static boolean hasStoredColor(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(COLOR_TAG);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.colorvariants.eyedropper.tooltip.1"));
        tooltip.add(Component.translatable("item.colorvariants.eyedropper.tooltip.2"));

        if (hasStoredColor(stack)) {
            ColorTransform transform = getStoredColor(stack);
            tooltip.add(Component.translatable("item.colorvariants.eyedropper.stored",
                    String.format("H:%.0f S:%.2f B:%.2f",
                            transform.getHueShift(),
                            transform.getSaturation(),
                            transform.getBrightness())));
        } else {
            tooltip.add(Component.translatable("item.colorvariants.eyedropper.empty"));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasStoredColor(stack);
    }
}
