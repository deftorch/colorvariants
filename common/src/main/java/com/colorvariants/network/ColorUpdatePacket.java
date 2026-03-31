package com.colorvariants.network;

import com.colorvariants.Constants;
import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Packet sent from client to server to update a block's color.
 */
public class ColorUpdatePacket {

    private static final int MAX_DISTANCE_SQ = 64; // 8 blocks squared

    public final BlockPos pos;
    public final ColorTransform transform;

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
            if (player == null) return;

            // 1. Distance check validation
            if (player.distanceToSqr(Vec3.atCenterOf(packet.pos)) > MAX_DISTANCE_SQ) {
                Constants.LOG.warn("Player {} tried to color block out of range", player.getName());
                return;
            }

            ServerLevel level = player.serverLevel();

            // 2. Save data to Manager
            ColorTransformManager manager = ColorTransformManager.get(level);
            manager.setTransform(packet.pos, packet.transform);

            // 3. FORCE UPDATE BLOCKSTATE
            level.sendBlockUpdated(packet.pos, level.getBlockState(packet.pos), level.getBlockState(packet.pos), 3);

            // 4. Handle Block Entity
            BlockEntity blockEntity = level.getBlockEntity(packet.pos);
            if (blockEntity instanceof ColoredBlockEntity coloredBE) {
                coloredBE.setTransform(packet.transform);
                coloredBE.setChanged();
            } else {
                try {
                    ColoredBlockEntity newBE = new ColoredBlockEntity(packet.pos, level.getBlockState(packet.pos));
                    newBE.setTransform(packet.transform);
                    level.setBlockEntity(newBE);
                } catch (Exception e) {
                   // Ignore
                }
            }
        });
        
        ctx.setPacketHandled(true);
    }
}
