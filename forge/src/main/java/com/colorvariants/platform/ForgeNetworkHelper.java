package com.colorvariants.platform;

import com.colorvariants.ColorVariants;
import com.colorvariants.platform.services.INetworkContext;
import com.colorvariants.platform.services.INetworkHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeNetworkHelper implements INetworkHelper {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ColorVariants.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    @Override
    public <MSG> void registerClientbound(Class<MSG> messageType, BiConsumer<MSG, io.netty.buffer.ByteBuf> encoder,
            Function<io.netty.buffer.ByteBuf, MSG> decoder, BiConsumer<MSG, INetworkContext> handler) {
        CHANNEL.registerMessage(packetId++, messageType,
                (msg, buf) -> encoder.accept(msg, buf), // FriendlyByteBuf extends ByteBuf
                decoder::apply,
                (msg, ctxSupplier) -> {
                    INetworkContext context = new ForgeNetworkContext(ctxSupplier);
                    handler.accept(msg, context);
                });
    }

    @Override
    public <MSG> void registerServerbound(Class<MSG> messageType, BiConsumer<MSG, io.netty.buffer.ByteBuf> encoder,
            Function<io.netty.buffer.ByteBuf, MSG> decoder, BiConsumer<MSG, INetworkContext> handler) {
        CHANNEL.registerMessage(packetId++, messageType,
                (msg, buf) -> encoder.accept(msg, buf),
                decoder::apply,
                (msg, ctxSupplier) -> {
                    INetworkContext context = new ForgeNetworkContext(ctxSupplier);
                    handler.accept(msg, context);
                });
    }

    @Override
    public <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    @Override
    public <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    @Override
    public <MSG> void sendToAll(MSG message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }
}
