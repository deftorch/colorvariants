package com.colorvariants.platform;

import com.colorvariants.platform.services.INetworkContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ForgeNetworkContext implements INetworkContext {

    private final Supplier<NetworkEvent.Context> contextSupplier;

    public ForgeNetworkContext(Supplier<NetworkEvent.Context> contextSupplier) {
        this.contextSupplier = contextSupplier;
    }

    @Override
    public void enqueueWork(Runnable runnable) {
        contextSupplier.get().enqueueWork(runnable);
    }

    @Override
    public ServerPlayer getSender() {
        return contextSupplier.get().getSender();
    }

    @Override
    public void setPacketHandled(boolean handled) {
        contextSupplier.get().setPacketHandled(handled);
    }
}
