package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import java.util.function.Supplier;

public class SyncAppraiseSettingsPacket {
    private final int minTime;
    private final int maxTime;
    private final String finishCmdsJson;
    private final String minusCmd;

    public SyncAppraiseSettingsPacket(int minTime, int maxTime, String finishCmdsJson, String minusCmd) {
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.finishCmdsJson = finishCmdsJson;
        this.minusCmd = minusCmd;
    }

    public SyncAppraiseSettingsPacket(FriendlyByteBuf buf) {
        this.minTime = buf.readInt();
        this.maxTime = buf.readInt();
        this.finishCmdsJson = buf.readUtf();
        this.minusCmd = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.minTime);
        buf.writeInt(this.maxTime);
        buf.writeUtf(this.finishCmdsJson);
        buf.writeUtf(this.minusCmd);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TrashDataManager.minAppraiseTime = this.minTime;
            TrashDataManager.maxAppraiseTime = this.maxTime;
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<String>>(){}.getType();
            TrashDataManager.finishCommands = new com.google.gson.Gson().fromJson(this.finishCmdsJson, listType);
            TrashDataManager.minusMoneyCommand = this.minusCmd;

            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                TrashDataManager.save();
                PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), this);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}