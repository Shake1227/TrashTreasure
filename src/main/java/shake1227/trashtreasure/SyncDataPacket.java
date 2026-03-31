package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import java.util.function.Supplier;

public class SyncDataPacket {
    private final String moneyCmd;
    private final String sellCmdsJson;
    private final String registeredItemsJson;

    public SyncDataPacket(String moneyCmd, String sellCmdsJson, String registeredItemsJson) {
        this.moneyCmd = moneyCmd;
        this.sellCmdsJson = sellCmdsJson;
        this.registeredItemsJson = registeredItemsJson;
    }

    public SyncDataPacket(FriendlyByteBuf buf) {
        this.moneyCmd = buf.readUtf();
        this.sellCmdsJson = buf.readUtf();
        this.registeredItemsJson = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.moneyCmd);
        buf.writeUtf(this.sellCmdsJson);
        buf.writeUtf(this.registeredItemsJson);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TrashDataManager.moneyCommand = this.moneyCmd;
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<String>>(){}.getType();
            TrashDataManager.sellCommands = new com.google.gson.Gson().fromJson(this.sellCmdsJson, listType);
            java.lang.reflect.Type itemListType = new com.google.gson.reflect.TypeToken<java.util.List<TrashDataManager.TrashItemData>>(){}.getType();
            TrashDataManager.registeredItems = new com.google.gson.Gson().fromJson(this.registeredItemsJson, itemListType);

            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                TrashDataManager.save();
                PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), this);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}