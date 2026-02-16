package com.colorvariants.platform.services;

import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    <MSG> void registerClientbound(Class<MSG> messageType,
            java.util.function.BiConsumer<MSG, io.netty.buffer.ByteBuf> encoder,
            java.util.function.Function<io.netty.buffer.ByteBuf, MSG> decoder,
            java.util.function.BiConsumer<MSG, INetworkContext> handler);

    <MSG> void registerServerbound(Class<MSG> messageType,
            java.util.function.BiConsumer<MSG, io.netty.buffer.ByteBuf> encoder,
            java.util.function.Function<io.netty.buffer.ByteBuf, MSG> decoder,
            java.util.function.BiConsumer<MSG, INetworkContext> handler);

    <MSG> void sendToServer(MSG message);

    <MSG> void sendToPlayer(MSG message, ServerPlayer player);

    <MSG> void sendToAll(MSG message);
}
