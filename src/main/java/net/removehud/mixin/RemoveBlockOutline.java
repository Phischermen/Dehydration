package net.removehud.mixin;

import net.dehydration.init.ConfigInit;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(GameRenderer.class)
public abstract class RemoveBlockOutline {

    @Inject(at=@At("TAIL"), method = "shouldRenderBlockOutline", cancellable = true)
    public void renderOutline(CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigInit.CONFIG.highlightBlocks) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }

    }
}
