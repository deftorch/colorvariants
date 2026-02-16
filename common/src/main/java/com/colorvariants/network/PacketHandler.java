package com.colorvariants.network;

import com.colorvariants.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles network packet registration and sending.
 */
public class PacketHandler {

    /**
     * Registers all packets.
     */
    public static void register() {
        Services.NETWORK.registerServerbound(
                ColorUpdatePacket.class,
                (msg, buf) -> ColorUpdatePacket.encode(msg, new FriendlyByteBuf(buf)),
                (buf) -> ColorUpdatePacket.decode(new FriendlyByteBuf(buf)),
                ColorUpdatePacket::handle);

        Services.NETWORK.registerClientbound(
                ColorSyncPacket.class,
                (msg, buf) -> ColorSyncPacket.encode(msg, new FriendlyByteBuf(buf)),
                (buf) -> ColorSyncPacket.decode(new FriendlyByteBuf(buf)),
                ColorSyncPacket::handle);

        Services.NETWORK.registerServerbound(
                AreaColorUpdatePacket.class,
                (msg, buf) -> AreaColorUpdatePacket.encode(msg, new FriendlyByteBuf(buf)),
                (buf) -> AreaColorUpdatePacket.decode(new FriendlyByteBuf(buf)),
                AreaColorUpdatePacket::handle);
    }

    /**
     * Sends a packet to the server.
     */
    public static <MSG> void sendToServer(MSG message) {
        Services.NETWORK.sendToServer(message);
    }

    /**
     * Sends a packet to a specific player.
     */
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        Services.NETWORK.sendToPlayer(message, player);
    }

    /**
     * Sends a packet to all players.
     */
    public static <MSG> void sendToAll(MSG message) {
        Services.NETWORK.sendToAll(message);
    }
}
