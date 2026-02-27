package com.colorvariants.network;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet for updating colors of multiple blocks in an area.
 */
public class AreaColorUpdatePacket {

    private static final int MAX_DISTANCE = 64;
    private static final double MAX_DISTANCE_SQR = MAX_DISTANCE * MAX_DISTANCE;
    private static final int MAX_AREA_SIZE = 32768; // approx 32x32x32

    private final List<BlockPos> positions;
    private final ColorTransform transform;
    private final boolean sameTypeOnly;

    public AreaColorUpdatePacket(List<BlockPos> positions, ColorTransform transform, boolean sameTypeOnly) {
        this.positions = positions;
        this.transform = transform;
        this.sameTypeOnly = sameTypeOnly;
    }

    /**
     * Encodes the packet to a buffer.
     */
    public static void encode(AreaColorUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.positions.size());

        for (BlockPos pos : packet.positions) {
            buf.writeBlockPos(pos);
        }

        buf.writeFloat(packet.transform.getHueShift());
        buf.writeFloat(packet.transform.getSaturation());
        buf.writeFloat(packet.transform.getBrightness());

        buf.writeBoolean(packet.sameTypeOnly);
    }

    /**
     * Decodes the packet from a buffer.
     */
    public static AreaColorUpdatePacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        // Security: Limit collection size allocation
        if (size > MAX_AREA_SIZE) {
             size = MAX_AREA_SIZE; // Cap it to avoid OOM attack
        }

        List<BlockPos> positions = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            positions.add(buf.readBlockPos());
        }

        float hue = buf.readFloat();
        float saturation = buf.readFloat();
        float brightness = buf.readFloat();
        ColorTransform transform = new ColorTransform(hue, saturation, brightness);

        boolean sameTypeOnly = buf.readBoolean();

        return new AreaColorUpdatePacket(positions, transform, sameTypeOnly);
    }

    /**
     * Handles the packet on the server side.
     */
    public static void handle(AreaColorUpdatePacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
                return;

            // Security: Max blocks check
            if (packet.positions.size() > MAX_AREA_SIZE) {
                return;
            }

            // Security: Permission
            if (!player.mayBuild()) {
                return;
            }

            Level world = player.level();
            ColorTransformManager manager = ColorTransformManager.get(world);

            // Security: Validate first position distance (optimization)
            if (!packet.positions.isEmpty()) {
                if (player.distanceToSqr(Vec3.atCenterOf(packet.positions.get(0))) > MAX_DISTANCE_SQR) {
                     return;
                }
            }

            if (packet.sameTypeOnly && !packet.positions.isEmpty()) {
                // Get the block type from the first position
                // Check if loaded
                BlockPos firstPos = packet.positions.get(0);
                if (!world.isLoaded(firstPos)) return;

                var firstBlockState = world.getBlockState(firstPos);

                // Filter positions to only include same block type and validate distance/loaded
                List<BlockPos> filteredPositions = new ArrayList<>();
                for (BlockPos pos : packet.positions) {
                    if (world.isLoaded(pos) &&
                        player.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_DISTANCE_SQR &&
                        world.getBlockState(pos).getBlock() == firstBlockState.getBlock()) {
                        filteredPositions.add(pos);
                    }
                }

                // Apply color to filtered positions
                for (BlockPos pos : filteredPositions) {
                    manager.setTransform(pos, packet.transform);
                }
            } else {
                // Apply to all valid positions
                for (BlockPos pos : packet.positions) {
                    if (world.isLoaded(pos) && player.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_DISTANCE_SQR) {
                        manager.setTransform(pos, packet.transform);
                    }
                }
            }

            // Sync to all clients (this might be spammy, ideally verify if needed)
            for (BlockPos pos : packet.positions) {
                 if (world.isLoaded(pos) && player.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_DISTANCE_SQR) {
                    ColorSyncPacket syncPacket = new ColorSyncPacket(pos, packet.transform);
                    PacketHandler.sendToAll(syncPacket);
                 }
            }
        });

        ctx.setPacketHandled(true);
    }
}
