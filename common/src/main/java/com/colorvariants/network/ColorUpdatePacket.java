package com.colorvariants.network;

import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.function.Supplier;

/**
 * Packet sent from client to server to update a block's color.
 */
public class ColorUpdatePacket {

    private final BlockPos pos;
    private final ColorTransform transform;

    public ColorUpdatePacket(BlockPos pos, ColorTransform transform) {
        this.pos = pos;
        this.transform = transform;
    }

    /**
     * Encodes the packet to a buffer.
     */
    public static void encode(ColorUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeFloat(packet.transform.getHueShift());
        buf.writeFloat(packet.transform.getSaturation());
        buf.writeFloat(packet.transform.getBrightness());
    }

    /**
     * Decodes the packet from a buffer.
     */
    public static ColorUpdatePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        float hue = buf.readFloat();
        float sat = buf.readFloat();
        float bright = buf.readFloat();

        return new ColorUpdatePacket(pos, new ColorTransform(hue, sat, bright));
    }

    // Validation constants
    private static final double MAX_DISTANCE = 64.0;
    private static final double MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;

    /**
     * Handles the packet on the server side.
     */
    public static void handle(ColorUpdatePacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();

            // Security check: Validate distance
            if (player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > MAX_DISTANCE_SQ) {
                com.colorvariants.Constants.LOG.warn("Player {} tried to update color too far away: {}", player.getName().getString(), packet.pos);
                return;
            }

            // Security check: Validate permission
            if (!player.hasPermissions(2) && !level.hasChunkAt(packet.pos)) {
                 com.colorvariants.Constants.LOG.warn("Player {} tried to update unloaded chunk: {}", player.getName().getString(), packet.pos);
                 return;
            }

            // 1. Save data to Manager
            ColorTransformManager manager = ColorTransformManager.get(level);
            manager.setTransform(packet.pos, packet.transform);

            // 2. FORCE UPDATE BLOCKSTATE (Important for client to know about changes)
            // Send block update to rerender chunk
            level.sendBlockUpdated(packet.pos, level.getBlockState(packet.pos), level.getBlockState(packet.pos), 3);

            // 3. Handle Block Entity if needed (though MixinBlockBehavior should handle capability)
            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            if (blockEntity instanceof ColoredBlockEntity coloredBE) {
                coloredBE.setTransform(packet.transform);
                coloredBE.setChanged();
            } else {
                // Should be handled by MixinBlockBehavior allowing the BE to exist
                // But we can try to set it if it's missing (might fail without mixin)
                try {
                    ColoredBlockEntity newBE = new ColoredBlockEntity(packet.pos, level.getBlockState(packet.pos));
                    newBE.setTransform(packet.transform);
                    level.setBlockEntity(newBE);
                } catch (Exception e) {
                   // Ignore if we can't set it (e.g. vanilla block checks)
                }
            }
        });
        
        ctx.setPacketHandled(true);
    }
}
