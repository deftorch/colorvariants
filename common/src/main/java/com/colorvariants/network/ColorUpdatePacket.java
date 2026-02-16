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

    /**
     * Handles the packet on the server side.
     */
    public static void handle(ColorUpdatePacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
                return;

            ServerLevel level = player.serverLevel();

            // Update the manager
            ColorTransformManager manager = ColorTransformManager.get(level);
            manager.setTransform(packet.pos, packet.transform);

            // Create or get block entity
            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            if (!(blockEntity instanceof ColoredBlockEntity)) {
                // Create new block entity
                ColoredBlockEntity coloredBE = new ColoredBlockEntity(
                        packet.pos,
                        level.getBlockState(packet.pos));
                coloredBE.setTransform(packet.transform);
                level.setBlockEntity(coloredBE);
            } else {
                // Update existing block entity
                ((ColoredBlockEntity) blockEntity).setTransform(packet.transform);
            }

            // Sync to all clients
            ColorSyncPacket syncPacket = new ColorSyncPacket(packet.pos, packet.transform);
            PacketHandler.sendToAll(syncPacket);
        });

        ctx.setPacketHandled(true);
    }
}
