package net.munchies;

import net.fabricmc.api.ClientModInitializer;
import net.munchies.network.MunchiesClientPacket;

public class MunchiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MunchiesClientPacket.init();
    }
}
