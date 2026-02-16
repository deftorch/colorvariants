package com.colorvariants;

import net.fabricmc.api.ModInitializer;

public class ColorVariantsFabric implements ModInitializer {

    public static net.minecraft.server.MinecraftServer SERVER;

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        ColorVariants.init();

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING
                .register(server -> SERVER = server);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING
                .register(server -> SERVER = null);

        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess, environment) -> {
                    com.colorvariants.command.ColorCommand.register(dispatcher);
                });
    }
}
