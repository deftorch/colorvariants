package com.colorvariants.item;

import com.colorvariants.client.gui.AreaColorPickerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.nbt.CompoundTag;
import java.util.Optional;
import javax.annotation.Nullable;

import java.util.List;

/**
 * Tool for selecting and coloring multiple blocks in an area.
 */
public class AreaSelectorItem extends Item {

    private static final String NBT_FIRST_POS = "FirstPos";
    private static final String NBT_SECOND_POS = "SecondPos";

    public AreaSelectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide) {
            HitResult hitResult = Minecraft.getInstance().hitResult;

            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                BlockPos pos = blockHit.getBlockPos();

                if (player.isShiftKeyDown()) {
                    // Second position
                    setSecondPos(stack, pos);
                    player.displayClientMessage(
                            Component.translatable("item.colorvariants.area_selector.second_pos",
                                    pos.getX(), pos.getY(), pos.getZ()),
                            true);

                    // If both positions set, open GUI
                    if (getFirstPos(stack).isPresent()) {
                        openAreaGUI(world, player, stack);
                    }
                } else {
                    // First position
                    setFirstPos(stack, pos);
                    clearSecondPos(stack);
                    player.displayClientMessage(
                            Component.translatable("item.colorvariants.area_selector.first_pos",
                                    pos.getX(), pos.getY(), pos.getZ()),
                            true);
                }
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private void openAreaGUI(Level world, Player player, ItemStack stack) {
        Optional<BlockPos> first = getFirstPos(stack);
        Optional<BlockPos> second = getSecondPos(stack);
        if (first.isPresent() && second.isPresent()) {
            Minecraft.getInstance().setScreen(
                    new AreaColorPickerScreen(first.get(), second.get()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.1"));
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.2"));
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.3"));
    }

    public static void reset(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_FIRST_POS);
        tag.remove(NBT_SECOND_POS);
    }

    public Optional<BlockPos> getFirstPos(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_FIRST_POS)) return Optional.empty();
        int[] coords = tag.getIntArray(NBT_FIRST_POS);
        if (coords.length < 3) return Optional.empty();
        return Optional.of(new BlockPos(coords[0], coords[1], coords[2]));
    }

    public void setFirstPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putIntArray(NBT_FIRST_POS, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    public Optional<BlockPos> getSecondPos(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_SECOND_POS)) return Optional.empty();
        int[] coords = tag.getIntArray(NBT_SECOND_POS);
        if (coords.length < 3) return Optional.empty();
        return Optional.of(new BlockPos(coords[0], coords[1], coords[2]));
    }

    public void setSecondPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putIntArray(NBT_SECOND_POS, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    public void clearSecondPos(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_SECOND_POS);
    }
}
