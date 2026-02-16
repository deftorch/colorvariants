package com.colorvariants.platform;

import com.colorvariants.platform.services.INetworkContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.PacketListener;

public class FabricNetworkContext implements INetworkContext {

    private final ServerPlayer player;
    private final PacketListener handler;

    public FabricNetworkContext(PacketListener handler, ServerPlayer player) {
        this.handler = handler;
        this.player = player;
    }

    // Constructor for client side where player might be null initially or obtained
    // via client
    public FabricNetworkContext(PacketListener handler, Object client) {
        this.handler = handler;
        this.player = null;
    }

    @Override
    public void enqueueWork(Runnable runnable) {
        // Fabric networking already handles threading if we use .execute() in the
        // receiver.
        // But if the runnables calls enqueueWork, we should just run it or delegate.
        runnable.run();
    }

    @Override
    public ServerPlayer getSender() {
        return player;
    }

    @Override
    public void setPacketHandled(boolean handled) {
        // No-op for Fabric
    }
}
