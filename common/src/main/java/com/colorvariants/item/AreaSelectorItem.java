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
import javax.annotation.Nullable;

import java.util.List;

/**
 * Tool for selecting and coloring multiple blocks in an area.
 */
public class AreaSelectorItem extends Item {

    public AreaSelectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide) {
            HitResult hitResult = Minecraft.getInstance().hitResult;

            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                BlockPos pos = blockHit.getBlockPos();
                ItemStack stack = player.getItemInHand(hand);
                net.minecraft.nbt.CompoundTag tag = stack.getOrCreateTag();

                if (player.isShiftKeyDown()) {
                    // Second position
                    AreaSelectorState.setSecondPos(tag, pos);
                    player.displayClientMessage(
                            Component.translatable("item.colorvariants.area_selector.second_pos",
                                    pos.getX(), pos.getY(), pos.getZ()),
                            true);

                    // If both positions set, open GUI
                    if (AreaSelectorState.getFirstPos(tag).isPresent()) {
                        openAreaGUI(world, player, stack);
                    }
                } else {
                    // First position
                    AreaSelectorState.setFirstPos(tag, pos);
                    AreaSelectorState.removeSecondPos(tag);
                    player.displayClientMessage(
                            Component.translatable("item.colorvariants.area_selector.first_pos",
                                    pos.getX(), pos.getY(), pos.getZ()),
                            true);
                }
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private void openAreaGUI(Level world, Player player, ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = stack.getOrCreateTag();
        java.util.Optional<BlockPos> p1 = AreaSelectorState.getFirstPos(tag);
        java.util.Optional<BlockPos> p2 = AreaSelectorState.getSecondPos(tag);

        if (p1.isPresent() && p2.isPresent()) {
            Minecraft.getInstance().setScreen(
                    new AreaColorPickerScreen(p1.get(), p2.get()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.1"));
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.2"));
        tooltip.add(Component.translatable("item.colorvariants.area_selector.tooltip.3"));
    }

    public static void reset() {
        AreaSelectorState.reset();
    }

    public java.util.Optional<BlockPos> getFirstPos(ItemStack stack) {
        return AreaSelectorState.getFirstPos(stack.getOrCreateTag());
    }

    public void setFirstPos(ItemStack stack, BlockPos pos) {
        AreaSelectorState.setFirstPos(stack.getOrCreateTag(), pos);
    }

    public java.util.Optional<BlockPos> getSecondPos(ItemStack stack) {
        return AreaSelectorState.getSecondPos(stack.getOrCreateTag());
    }

    public void setSecondPos(ItemStack stack, BlockPos pos) {
        AreaSelectorState.setSecondPos(stack.getOrCreateTag(), pos);
    }
}
