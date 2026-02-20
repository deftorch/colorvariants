package com.colorvariants.network;

import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.core.ColorTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.function.Supplier;

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
    /**
     * Handles the packet on the client side.
     * Note: This is a client-side packet, so server validation (MAX_DISTANCE) is not applicable here.
     * The server performs validation before sending this packet.
     */
    public static void handle(ColorSyncPacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null)
                return;

            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            if (!(blockEntity instanceof ColoredBlockEntity)) {
                // Create new block entity on client
                ColoredBlockEntity coloredBE = new ColoredBlockEntity(
                        packet.pos,
                        level.getBlockState(packet.pos));
                coloredBE.setTransform(packet.transform);
                level.setBlockEntity(coloredBE);
            } else {
                // Update existing block entity
                ((ColoredBlockEntity) blockEntity).setTransform(packet.transform);
            }
        });

        ctx.setPacketHandled(true);
    }
}
