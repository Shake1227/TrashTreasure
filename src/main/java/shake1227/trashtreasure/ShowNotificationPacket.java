package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;

public class ShowNotificationPacket {
    private final String category;
    private final String componentJson;

    public ShowNotificationPacket(String category, String componentJson) {
        this.category = category;
        this.componentJson = componentJson;
    }

    public ShowNotificationPacket(FriendlyByteBuf buf) {
        this.category = buf.readUtf();
        this.componentJson = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.category);
        buf.writeUtf(this.componentJson);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientExecutor.run(this.category, this.componentJson));
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientExecutor {
        public static Runnable run(String category, String componentJson) {
            return () -> shake1227.trashtreasure.client.ClientPacketHandler.handleShowNotification(category, componentJson);
        }
    }
}