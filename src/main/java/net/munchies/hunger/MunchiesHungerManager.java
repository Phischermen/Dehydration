package net.munchies.hunger;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.munchies.MunchiesMain;

public class MunchiesHungerManager extends HungerManager {
    private PlayerEntity player;

    public MunchiesHungerManager(PlayerEntity player){
        this.player = player;
    }

    @Override
    public void add(int hunger, float saturation){
        super.add(hunger, 0f);
    }

    @Override
    public void setSaturationLevel(float saturationLevel){
        // Disable the saturation system.
        super.setSaturationLevel(0);
    }

    @Override
    public float getSaturationLevel(){
        // Disable the saturation system.
        return 0;
    }

    @Override
    public void setFoodLevel(int foodLevel){
        // Clamp food level to 20. This is necessary because 'isNotFull' is normally used when deciding to regen food.
        foodLevel = Math.min(foodLevel, 20);
        super.setFoodLevel(foodLevel);
    }

    @Override
    public boolean isNotFull() {
        // Player may now eat at all times.
        return true;
    }

    @Override
    public void eat(Item item, ItemStack stack) {
        if (!item.isFood()) return;
        var foodComp = item.getFoodComponent();
        if (foodComp == null) return;
        var hp = 0;
        for (int i = 0; i < MunchiesMain.MUNCHIES_TEMPLATES.size(); i++) {
            if (MunchiesMain.MUNCHIES_TEMPLATES.get(i).containsItem(item)){
                hp = MunchiesMain.MUNCHIES_TEMPLATES.get(i).getHealAmount();
            }
            if (hp != 0){
                heal(hp);
                break;
            }
        }
        super.eat(item, stack);
    }

    private void heal(int amount){
        player.heal(amount);
    }
}
