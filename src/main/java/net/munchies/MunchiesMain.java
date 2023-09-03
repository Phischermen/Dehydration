package net.munchies;

import net.fabricmc.api.ModInitializer;
import net.munchies.api.MunchiesTemplate;
import net.dehydration.api.HydrationTemplate;
import net.munchies.init.JsonReaderInit;
import net.munchies.network.MunchiesServerPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MunchiesMain implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("Munchies");

    public static final List<MunchiesTemplate> MUNCHIES_TEMPLATES = new ArrayList<MunchiesTemplate>();

    @Override
    public void onInitialize() {
        MunchiesServerPacket.init();
        JsonReaderInit.init();
    }
}

