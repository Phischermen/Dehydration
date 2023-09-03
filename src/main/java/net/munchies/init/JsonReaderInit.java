package net.munchies.init;

import net.munchies.MunchiesMain;
import net.munchies.data.DataLoader;
import net.munchies.network.MunchiesServerPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class JsonReaderInit {
    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DataLoader());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) {
                for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++)
                    MunchiesServerPacket.writeS2CMunchiesTemplateSyncPacket(server.getPlayerManager().getPlayerList().get(i));
                MunchiesMain.LOGGER.info("Finished reload on {}", Thread.currentThread());
            } else
                MunchiesMain.LOGGER.error("Failed to reload on {}", Thread.currentThread());
        });
    }
}
