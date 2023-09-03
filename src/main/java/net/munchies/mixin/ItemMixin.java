package net.munchies.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.munchies.MunchiesMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private int getMaxCount(CallbackInfoReturnable ci){
        var item = ((Item) (Object) this);
        if (item.isFood()){
            Integer stackSizeOverride = -1;
            MunchiesMain.LOGGER.debug("Searching templates for food.");
            for (int i = 0; i < MunchiesMain.MUNCHIES_TEMPLATES.size(); i++) {
                if (MunchiesMain.MUNCHIES_TEMPLATES.get(i).containsItem(item)){
                    stackSizeOverride = MunchiesMain.MUNCHIES_TEMPLATES.get(i).getStackSizeOverride();
                    MunchiesMain.LOGGER.debug("Template found " + item + stackSizeOverride);
                }
            }
            if (stackSizeOverride != -1){
                ci.setReturnValue(stackSizeOverride);
            }
        }
        return 0;
    }
}
