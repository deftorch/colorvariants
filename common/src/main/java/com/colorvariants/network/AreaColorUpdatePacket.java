package com.colorvariants.network;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import com.colorvariants.core.UndoRedoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Packet for updating colors of multiple blocks in an area.
 */
public class AreaColorUpdatePacket {

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

    // Validation constants
    private static final double MAX_DISTANCE = 64.0;
    private static final double MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;
    private static final int MAX_BLOCKS = 32768; // Max blocks in a 32x32x32 area

    /**
     * Handles the packet on the server side.
     */
    /**
     * Handles the packet on the server side.
     */
    public static void handle(AreaColorUpdatePacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
                return;

            // Security check: Validate size
            if (packet.positions.size() > MAX_BLOCKS) {
                com.colorvariants.Constants.LOG.warn("Player {} tried to update too many blocks: {}", player.getName().getString(), packet.positions.size());
                return;
            }

            // Security check: Validate distance of first block (basic check)
            if (!packet.positions.isEmpty()) {
                BlockPos first = packet.positions.get(0);
                if (player.distanceToSqr(first.getX() + 0.5, first.getY() + 0.5, first.getZ() + 0.5) > MAX_DISTANCE_SQ) {
                    com.colorvariants.Constants.LOG.warn("Player {} tried to area update too far away: {}", player.getName().getString(), first);
                    return;
                }
            }

            Level world = player.level();

            // Additional check: Ensure all chunks are loaded to prevent loading exploits
            for (BlockPos pos : packet.positions) {
                if (!world.hasChunkAt(pos)) {
                     com.colorvariants.Constants.LOG.warn("Player {} tried to update unloaded area: {}", player.getName().getString(), pos);
                     return;
                }
            }

            ColorTransformManager manager = ColorTransformManager.get(world);

            if (packet.sameTypeOnly && !packet.positions.isEmpty()) {
                // Get the block type from the first position
                var firstBlockState = world.getBlockState(packet.positions.get(0));

                // Filter positions to only include same block type
                List<BlockPos> filteredPositions = packet.positions.stream()
                        .filter(pos -> world.getBlockState(pos).getBlock() == firstBlockState.getBlock())
                        .toList();

                // Apply color to filtered positions
                for (BlockPos pos : filteredPositions) {
                    manager.setTransform(pos, packet.transform);
                }
            } else {
                // Apply to all positions
                for (BlockPos pos : packet.positions) {
                    manager.setTransform(pos, packet.transform);
                }
            }

            // Sync to all clients
            for (BlockPos pos : packet.positions) {
                ColorSyncPacket syncPacket = new ColorSyncPacket(pos, packet.transform);
                PacketHandler.sendToAll(syncPacket);
            }
        });

        ctx.setPacketHandled(true);
    }
}
