package shake1227.trashtreasure;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TrashTreasure.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        packetId = 0;
        INSTANCE.registerMessage(packetId++, SyncDataPacket.class, SyncDataPacket::encode, SyncDataPacket::new, SyncDataPacket::handle);
        INSTANCE.registerMessage(packetId++, shake1227.trashtreasure.OpenGuiPacket.class, shake1227.trashtreasure.OpenGuiPacket::encode, OpenGuiPacket::new, shake1227.trashtreasure.OpenGuiPacket::handle);
        INSTANCE.registerMessage(packetId++, OpenItemRegPacket.class, OpenItemRegPacket::encode, OpenItemRegPacket::new, OpenItemRegPacket::handle);
        INSTANCE.registerMessage(packetId++, shake1227.trashtreasure.OpenItemEditPacket.class, shake1227.trashtreasure.OpenItemEditPacket::encode, OpenItemEditPacket::new, shake1227.trashtreasure.OpenItemEditPacket::handle);
        INSTANCE.registerMessage(packetId++, OpenShopDeletePacket.class, OpenShopDeletePacket::encode, OpenShopDeletePacket::new, OpenShopDeletePacket::handle);
        INSTANCE.registerMessage(packetId++, DeleteShopPacket.class, DeleteShopPacket::encode, DeleteShopPacket::new, DeleteShopPacket::handle);
        INSTANCE.registerMessage(packetId++, OpenInterruptPacket.class, OpenInterruptPacket::encode, OpenInterruptPacket::new, OpenInterruptPacket::handle);
        INSTANCE.registerMessage(packetId++, CancelAppraisePacket.class, CancelAppraisePacket::encode, CancelAppraisePacket::new, CancelAppraisePacket::handle);
        INSTANCE.registerMessage(packetId++, SyncAppraiseSettingsPacket.class, SyncAppraiseSettingsPacket::encode, SyncAppraiseSettingsPacket::new, SyncAppraiseSettingsPacket::handle);
        INSTANCE.registerMessage(packetId++, SyncProgressPacket.class, SyncProgressPacket::encode, SyncProgressPacket::new, SyncProgressPacket::handle);
        INSTANCE.registerMessage(packetId++, AppraiseFinishAnimPacket.class, AppraiseFinishAnimPacket::encode, AppraiseFinishAnimPacket::new, AppraiseFinishAnimPacket::handle);
        INSTANCE.registerMessage(packetId++, ShowNotificationPacket.class, ShowNotificationPacket::encode, ShowNotificationPacket::new, ShowNotificationPacket::handle);
    }
}