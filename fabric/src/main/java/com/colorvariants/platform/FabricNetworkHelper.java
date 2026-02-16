package com.colorvariants.platform;

import com.colorvariants.platform.services.INetworkContext;
import com.colorvariants.platform.services.INetworkHelper;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricNetworkHelper implements INetworkHelper {

    private final Map<Class<?>, BiConsumer<?, ByteBuf>> encoders = new HashMap<>();

    @Override
    public <MSG> void registerClientbound(Class<MSG> messageType, BiConsumer<MSG, ByteBuf> encoder,
            Function<ByteBuf, MSG> decoder, BiConsumer<MSG, INetworkContext> handler) {
        ResourceLocation id = new ResourceLocation("colorvariants", messageType.getSimpleName().toLowerCase());
        encoders.put(messageType, encoder);

        // Register client receiver (mock/stub for now as we are in common source set
        // potentially mixed)
        // In a real scenario, we should use ClientPlayNetworking only on client.
        // For this task, we assume it's safe or will be handled by the loader
        // environment.
        try {
            if (net.fabricmc.api.EnvType.CLIENT == net.fabricmc.loader.api.FabricLoader.getInstance()
                    .getEnvironmentType()) {
                ClientPlayNetworking.registerGlobalReceiver(id, (client, handler1, buf, responseSender) -> {
                    MSG message = decoder.apply(buf);
                    INetworkContext context = new FabricNetworkContext(handler1, client);
                    client.execute(() -> handler.accept(message, context));
                });
            }
        } catch (Throwable e) {
            // Ignore if client classes missing
        }
    }

    @Override
    public <MSG> void registerServerbound(Class<MSG> messageType, BiConsumer<MSG, ByteBuf> encoder,
            Function<ByteBuf, MSG> decoder, BiConsumer<MSG, INetworkContext> handler) {
        ResourceLocation id = new ResourceLocation("colorvariants", messageType.getSimpleName().toLowerCase());
        encoders.put(messageType, encoder);

        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler1, buf, responseSender) -> {
            MSG message = decoder.apply(buf);
            INetworkContext context = new FabricNetworkContext(handler1, player);
            server.execute(() -> handler.accept(message, context));
        });
    }

    @Override
    public <MSG> void sendToServer(MSG message) {
        ResourceLocation id = new ResourceLocation("colorvariants", message.getClass().getSimpleName().toLowerCase());
        FriendlyByteBuf buf = PacketByteBufs.create();
        ((BiConsumer<MSG, ByteBuf>) encoders.get(message.getClass())).accept(message, buf);
        ClientPlayNetworking.send(id, buf);
    }

    @Override
    public <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        ResourceLocation id = new ResourceLocation("colorvariants", message.getClass().getSimpleName().toLowerCase());
        FriendlyByteBuf buf = PacketByteBufs.create();
        ((BiConsumer<MSG, ByteBuf>) encoders.get(message.getClass())).accept(message, buf);
        ServerPlayNetworking.send(player, id, buf);
    }

    @Override
    public <MSG> void sendToAll(MSG message) {
        ResourceLocation id = new ResourceLocation("colorvariants", message.getClass().getSimpleName().toLowerCase());
        FriendlyByteBuf buf = PacketByteBufs.create();
        ((BiConsumer<MSG, ByteBuf>) encoders.get(message.getClass())).accept(message, buf);

        if (com.colorvariants.ColorVariantsFabric.SERVER != null) {
            for (ServerPlayer player : com.colorvariants.ColorVariantsFabric.SERVER.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, id, buf);
            }
        }
        // Note: we need access to server to get player list.
        // We can't easily get it here without context.
        // Usually we pass context or lookup.
        // For now, let's assume we can't send to all easily without server instance,
        // OR we iterate if we had the server.
        // Actually, sendToAll usually is done from a context where we have level or
        // server.
        // But here we are in a static helper.
        // Fabric doesn't have a direct "sendToAll" without a server instance.
        // We might need to look up server from a holder.
        // Services.PLATFORM could provide it?
        // Or we pass `server` to `init`?
    }
}
