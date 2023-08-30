package net.dehydration.mixin;

import java.util.Optional;

import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.dehydration.DehydrationMain;
import net.dehydration.access.ThirstManagerAccess;
import net.dehydration.init.ConfigInit;
import net.dehydration.init.TagInit;
import net.dehydration.misc.ThirstTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "finishUsing", at = @At(value = "HEAD"))
    private void finishUsingMixin(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (!stack.isFood() && user instanceof PlayerEntity player) {
            int thirstQuench = 0;
            if (stack.isIn(TagInit.HYDRATING_STEW)) {
                thirstQuench = ConfigInit.CONFIG.stew_thirst_quench;
            } else if (stack.isIn(TagInit.HYDRATING_FOOD)) {
                thirstQuench = ConfigInit.CONFIG.food_thirst_quench;
            } else if (stack.isIn(TagInit.HYDRATING_DRINKS)) {
                thirstQuench = ConfigInit.CONFIG.drinks_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_STEW)) {
                thirstQuench = ConfigInit.CONFIG.stronger_stew_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_FOOD)) {
                thirstQuench = ConfigInit.CONFIG.stronger_food_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_DRINKS)) {
                thirstQuench = ConfigInit.CONFIG.stronger_drinks_thirst_quench;
            }
            for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
                if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(stack.getItem())) {
                    thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                    break;
                }
            }
            if (thirstQuench > 0) {
                ((ThirstManagerAccess) player).getThirstManager().add(thirstQuench);
            }
        }
    }

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void getTooltipDataMixin(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> info) {
        if (ConfigInit.CONFIG.thirst_preview) {
            int thirstQuench = 0;
            if (stack.isIn(TagInit.HYDRATING_STEW)) {
                thirstQuench = ConfigInit.CONFIG.stew_thirst_quench;
            } else if (stack.isIn(TagInit.HYDRATING_FOOD)) {
                thirstQuench = ConfigInit.CONFIG.food_thirst_quench;
            } else if (stack.isIn(TagInit.HYDRATING_DRINKS)) {
                thirstQuench = ConfigInit.CONFIG.drinks_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_STEW)) {
                thirstQuench = ConfigInit.CONFIG.stronger_stew_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_FOOD)) {
                thirstQuench = ConfigInit.CONFIG.stronger_food_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_DRINKS)) {
                thirstQuench = ConfigInit.CONFIG.stronger_drinks_thirst_quench;
            }
            for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
                if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(stack.getItem())) {
                    thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                    break;
                }
            }

            if (thirstQuench > 0) {
                info.setReturnValue(Optional.of(new ThirstTooltipData(0, thirstQuench)));
            }
        }
    }


    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private int getMaxCount(CallbackInfoReturnable ci){
        Item[] stackableFoods = {Items.COOKIE, Items.MELON_SLICE};
        var item = ((Item) (Object) this);
        if (item.isFood()){
            boolean isStackable = false;
            for (Item stackableFood : stackableFoods) {
                //if (Item.getRawId(item) == Item.getRawId(stackableFood)){
                if (item.equals(stackableFood)){
                    isStackable = true;
                    break;
                }
            }
            //isStackable = Item.getRawId(item) ==(Items.COOKIE) || item.equals(Items.MELON);
            ci.setReturnValue(isStackable ? 16 : 1);
        }
        return 0;
    }
}
