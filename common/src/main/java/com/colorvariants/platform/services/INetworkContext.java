package com.colorvariants.platform.services;

import net.minecraft.server.level.ServerPlayer;

public interface INetworkContext {
    void enqueueWork(Runnable runnable);

    ServerPlayer getSender();

    void setPacketHandled(boolean handled);
}
