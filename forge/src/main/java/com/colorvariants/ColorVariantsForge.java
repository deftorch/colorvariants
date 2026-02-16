package com.colorvariants;

import com.colorvariants.platform.ForgeRegistryHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ColorVariantsForge {

    public ColorVariantsForge() {
        // Use Forge to bootstrap the Common mod.
        ColorVariants.init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeRegistryHelper.register(bus);

        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                com.colorvariants.platform.ForgeConfigHelper.CONFIG_SPEC);

        bus.addListener(this::clientSetup);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void clientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                    ColorVariants.COLORED_BLOCK_ENTITY.get(),
                    com.colorvariants.client.renderer.ColoredBlockRenderer::new);
        });
    }

    public void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        com.colorvariants.command.ColorCommand.register(event.getDispatcher());
    }
}