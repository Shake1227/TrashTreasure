package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;

public class SyncProgressPacket {
    private final int current;
    private final int total;

    public SyncProgressPacket(int current, int total) {
        this.current = current;
        this.total = total;
    }

    public SyncProgressPacket(FriendlyByteBuf buf) {
        this.current = buf.readInt();
        this.total = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.current);
        buf.writeInt(this.total);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientExecutor.run(this.current, this.total));
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientExecutor {
        public static Runnable run(int current, int total) {
            return () -> shake1227.trashtreasure.client.ClientPacketHandler.handleSyncProgress(current, total);
        }
    }
}