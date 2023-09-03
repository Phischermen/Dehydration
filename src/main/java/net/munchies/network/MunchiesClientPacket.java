package net.munchies.network;

import it.unimi.dsi.fastutil.ints.IntList;
import net.dehydration.DehydrationMain;
import net.dehydration.api.HydrationTemplate;
import net.dehydration.network.ThirstServerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.munchies.MunchiesMain;
import net.munchies.api.MunchiesTemplate;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MunchiesClientPacket {
    public static void init(){
        ClientPlayNetworking.registerGlobalReceiver(MunchiesServerPacket.MUNCHIES_TEMPLATE_SYNC, (client, handler, buffer, responseSender) -> {
            List<MunchiesTemplate> munchiesTemplates = new ArrayList<MunchiesTemplate>();
            IntList intList = buffer.readIntList();
            for (int i = 0; i < intList.size(); i += 3) { // 3 because i is healAmount and i+1 is stackSize and i+2 is amount of items in the template.
                List<Item> items = new ArrayList<Item>();
                for (int u = 0; u < intList.getInt(i + 2); u++) {
                    items.add(Registries.ITEM.get(buffer.readIdentifier()));
                }
                munchiesTemplates.add(new MunchiesTemplate(intList.getInt(i), intList.getInt(i + 1), items));
            }
            client.execute(() -> {
                MunchiesMain.MUNCHIES_TEMPLATES.clear();
                MunchiesMain.MUNCHIES_TEMPLATES.addAll(munchiesTemplates);
            });
        });
    }
}
