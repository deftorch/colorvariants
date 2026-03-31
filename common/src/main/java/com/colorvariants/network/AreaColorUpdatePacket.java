package com.colorvariants.network;

import com.colorvariants.Constants;
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

    private static final int MAX_DISTANCE_SQ = 64; // 8 blocks squared
    private static final int MAX_AREA_VOLUME = 32768; // approx 32x32x32

    public final List<BlockPos> positions;
    public final ColorTransform transform;
    public final boolean sameTypeOnly;

    public AreaColorUpdatePacket(List<BlockPos> positions, ColorTransform transform, boolean sameTypeOnly) {
        this.positions = positions;
        this.transform = transform;
        this.sameTypeOnly = sameTypeOnly;
    }

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

    public static void handle(AreaColorUpdatePacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
                return;

            if (packet.positions.size() > MAX_AREA_VOLUME) {
                 Constants.LOG.warn("Player {} tried to color too many blocks", player.getName());
                 return;
            }

            for (BlockPos pos : packet.positions) {
                if (player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_DISTANCE_SQ) {
                    Constants.LOG.warn("Player {} tried to color block out of range", player.getName());
                    return;
                }
            }

            Level world = player.level();
            ColorTransformManager manager = ColorTransformManager.get(world);

            if (packet.sameTypeOnly && !packet.positions.isEmpty()) {
                var firstBlockState = world.getBlockState(packet.positions.get(0));

                List<BlockPos> filteredPositions = packet.positions.stream()
                        .filter(pos -> world.getBlockState(pos).getBlock() == firstBlockState.getBlock())
                        .toList();

                for (BlockPos pos : filteredPositions) {
                    manager.setTransform(pos, packet.transform);
                }
            } else {
                for (BlockPos pos : packet.positions) {
                    manager.setTransform(pos, packet.transform);
                }
            }

            for (BlockPos pos : packet.positions) {
                ColorSyncPacket syncPacket = new ColorSyncPacket(pos, packet.transform);
                PacketHandler.sendToAll(syncPacket);
            }
        });

        ctx.setPacketHandled(true);
    }
}
