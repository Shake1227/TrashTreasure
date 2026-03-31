package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;

public class AppraiseFinishAnimPacket {
    public AppraiseFinishAnimPacket() {}

    public AppraiseFinishAnimPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ClientExecutor::run);
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientExecutor {
        public static Runnable run() {
            return () -> shake1227.trashtreasure.client.ClientPacketHandler.handleAppraiseFinishAnim();
        }
    }
}