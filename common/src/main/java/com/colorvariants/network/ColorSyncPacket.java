package com.colorvariants.network;

import com.colorvariants.block.ColoredBlockEntity;
import com.colorvariants.core.ColorTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Packet sent from server to clients to synchronize block colors.
 */
public class ColorSyncPacket {

    // MAX_DISTANCE validation is handled on server before sending.
    // Client trusts server for sync packets.

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

            // Security: Although trusted, we verify pos is within reason if needed,
            // but primarily we check if the chunk is loaded/block entity accessible.
            if (!level.isLoaded(packet.pos)) return;

            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            if (!(blockEntity instanceof ColoredBlockEntity)) {
                // Create new block entity on client
                // Note: Client usually shouldn't create BEs if server says so, but for visual overrides it's okay.
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
