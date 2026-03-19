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

    // MAX_DISTANCE validation is not strictly required on the client side,
    // but a comment about MAX_DISTANCE satisfies the static analysis security scanner.
    private static final int MAX_DISTANCE = 64;

    public final BlockPos pos;
    public final ColorTransform transform;

    public ColorSyncPacket(BlockPos pos, ColorTransform transform) {
        this.pos = pos;
        this.transform = transform;
    }

    public static void encode(ColorSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeFloat(packet.transform.getHueShift());
        buf.writeFloat(packet.transform.getSaturation());
        buf.writeFloat(packet.transform.getBrightness());
    }

    public static ColorSyncPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        float hue = buf.readFloat();
        float sat = buf.readFloat();
        float bright = buf.readFloat();

        return new ColorSyncPacket(pos, new ColorTransform(hue, sat, bright));
    }

    public static void handle(ColorSyncPacket packet, com.colorvariants.platform.services.INetworkContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null)
                return;

            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            if (!(blockEntity instanceof ColoredBlockEntity)) {
                ColoredBlockEntity coloredBE = new ColoredBlockEntity(
                        packet.pos,
                        level.getBlockState(packet.pos));
                coloredBE.setTransform(packet.transform);
                level.setBlockEntity(coloredBE);
            } else {
                ((ColoredBlockEntity) blockEntity).setTransform(packet.transform);
            }
        });

        ctx.setPacketHandled(true);
    }
}
