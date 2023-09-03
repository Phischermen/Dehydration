package net.munchies.api;

import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MunchiesTemplate {
    private final int healAmount; // set to 0 to disable healing. set to negative to inflict damage.
    private final int stackSizeOverride; // set to -1 to disable override
    private final List<Item> items = new ArrayList<Item>();

    public MunchiesTemplate(int healAmount, int stackSize, List<Item> items) {
        this.healAmount = healAmount;
        this.stackSizeOverride = stackSize;
        this.items.addAll(items);
    }

    public int getHealAmount() {
        return healAmount;
    }

    public int getStackSizeOverride(){
        return stackSizeOverride;
    }

    public List<Item> getItems() {
        return this.items;
    }

    public boolean containsItem(Item item) {
        return this.items.contains(item);
    }
}
