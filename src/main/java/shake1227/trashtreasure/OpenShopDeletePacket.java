package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;

public class OpenShopDeletePacket {
    private final int entityId;

    public OpenShopDeletePacket(int entityId) {
        this.entityId = entityId;
    }

    public OpenShopDeletePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientExecutor.run(this.entityId));
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientExecutor {
        public static Runnable run(int entityId) {
            return () -> shake1227.trashtreasure.client.ClientPacketHandler.openShopDeleteGui(entityId);
        }
    }
}