package net.munchies.network;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.munchies.MunchiesMain;

public class MunchiesServerPacket {
    public static final Identifier MUNCHIES_TEMPLATE_SYNC = new Identifier("munchies", "munchies_template_sync");

    public static void init() {
    }

    public static void writeS2CMunchiesTemplateSyncPacket(ServerPlayerEntity serverPlayerEntity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        IntArrayList intList = new IntArrayList();
        MunchiesMain.MUNCHIES_TEMPLATES.forEach((template) -> {
            intList.add(template.getHealAmount());
            intList.add(template.getStackSizeOverride());
            intList.add(template.getItems().size());
        });
        buf.writeIntList(intList);

        MunchiesMain.MUNCHIES_TEMPLATES.forEach((template) -> {
            template.getItems().forEach((item) -> {
                buf.writeIdentifier(Registries.ITEM.getId(item));
            });
        });
        serverPlayerEntity.networkHandler.sendPacket(new CustomPayloadS2CPacket(MunchiesServerPacket.MUNCHIES_TEMPLATE_SYNC, buf));
    }
}
