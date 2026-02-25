package com.colorvariants.network;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.core.ColorTransformManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

/**
 * Packet sent from server to clients to synchronize block colors.
 */
public class ColorSyncPacket {

    private final BlockPos pos;
    private final ColorTransform transform;

    public ColorSyncPacket(BlockPos pos, ColorTransform transform) {
        this.pos = pos;
        this.transform = transform;
    }

    /**
     * Encodes the packet to a buffer.
     */
    public static void encode(ColorSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeFloat(packet.transform.getHueShift());
        buf.writeFloat(packet.transform.getSaturation());
        buf.writeFloat(packet.transform.getBrightness());
    }

    /**
     * Decodes the packet from a buffer.
     */
    public static ColorSyncPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        float hue = buf.readFloat();
        float sat = buf.readFloat();
        float bright = buf.readFloat();

        return new ColorSyncPacket(pos, new ColorTransform(hue, sat, bright));
    }

    /**
     * Handles the packet on the client side.
     */
    public static void handle(ColorSyncPacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null)
                return;

            ColorTransformManager.get(level).setTransform(packet.pos, packet.transform);

            // Force a re-render of the block
            // 3 = UPDATE_CLIENTS (notify listeners) | UPDATE_IMMEDIATE ??
            // Usually notifyNeighbors (1) | notifyListeners (2) = 3.
            // On client, setBlock usually triggers renderer update.
            // But we are not changing the block state, just the color data which is now external.
            // So we need to explicitly trigger a chunk update or block update.
            level.sendBlockUpdated(packet.pos, level.getBlockState(packet.pos), level.getBlockState(packet.pos), 3);
        });

        ctx.setPacketHandled(true);
    }
}
