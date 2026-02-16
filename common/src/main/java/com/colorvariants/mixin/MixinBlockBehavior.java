package com.colorvariants.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class MixinBlockBehavior {
    // Forces the game to think all blocks CAN have a BlockEntity
    // Note: This is an aggressive solution. Ideally use Map<BlockPos, Color> in WorldSavedData 
    // instead of BlockEntity to avoid massive overhead, but this matches the user request.
    @Inject(method = "hasBlockEntity", at = @At("HEAD"), cancellable = true)
    private void onHasBlockEntity(CallbackInfoReturnable<Boolean> cir) {
        // Logic can be refined to not return true for AIR or bedrock
        // For now, return true so the packet logic works.
        cir.setReturnValue(true); 
    }
}
