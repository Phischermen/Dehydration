package net.dehydration.mixin;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Item.Settings.class)
public abstract class ItemSettingsMixin {
    @Shadow
    FoodComponent foodComponent;

    @Inject(method = "food", at = @At("HEAD"))
    private void hookFood(FoodComponent foodComponent, CallbackInfoReturnable<Item.Settings> info) {
        if (foodComponent == null) return;
        var itemSettings = ((Item.Settings) (Object) this);
        // Ideally this would be configurable per object.
        itemSettings.maxCount(1);
    }

    @ModifyVariable(method = "maxCount", at = @At("HEAD"), argsOnly = true)
    private int hookMaxCount(int maxCount) {
        return foodComponent == null ? maxCount : 1;
    }
}
