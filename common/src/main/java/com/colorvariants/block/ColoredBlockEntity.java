package com.colorvariants.block;

import com.colorvariants.ColorVariants;
import com.colorvariants.core.ColorTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity that stores color transform data for a block.
 * This is dynamically attached to any block that has been colored.
 */
public class ColoredBlockEntity extends BlockEntity {

    private ColorTransform transform = ColorTransform.NONE;

    public ColoredBlockEntity(BlockPos pos, BlockState state) {
        super(ColorVariants.COLORED_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Gets the color transform for this block.
     */
    public ColorTransform getTransform() {
        return transform;
    }

    /**
     * Sets the color transform for this block.
     */
    public void setTransform(ColorTransform transform) {
        this.transform = transform;
        setChanged();

        // Sync to clients
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!transform.isNone()) {
            tag.put("ColorTransform", transform.save());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("ColorTransform")) {
            transform = ColorTransform.load(tag.getCompound("ColorTransform"));
        } else {
            transform = ColorTransform.NONE;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (!transform.isNone()) {
            tag.put("ColorTransform", transform.save());
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
