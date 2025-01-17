package net.compromisedhunger;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CompromisedHungerManager extends HungerManager {

    private PlayerEntity player;

    public CompromisedHungerManager(PlayerEntity player){
        this.player = player;
    }

    @Override
    public void addExhaustion(float exhaustion) {
        // Player gets exhausted twice as fast.
        super.addExhaustion(exhaustion * 2f);
    }

    @Override
    public boolean isNotFull() {
        // Player may now eat when health not full.
        return super.isNotFull() || player.getHealth() < player.getMaxHealth();
    }

    @Override
    public void eat(Item item, ItemStack stack) {
        if (!item.isFood()) return;
        var foodComp = item.getFoodComponent();
        if (foodComp == null) return;
        var hp = foodComp.getHunger() / 2;
        // Hp food can heal is capped to be equal to a normal health potion.
        if (hp > 4) hp = 4;
        heal(hp);
        super.eat(item, stack);
    }

    private void heal(int amount){
        player.heal(amount);
    }
}
