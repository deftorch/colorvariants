package com.colorvariants.mixin;

import com.colorvariants.core.ColorTransformManager;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void colorvariants$onDisconnect(CallbackInfo ci) {
        ColorTransformManager.clearClient();
    }
}
