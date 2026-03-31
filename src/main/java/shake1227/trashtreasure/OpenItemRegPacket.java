package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;

public class OpenItemRegPacket {
    private final int windowId;
    private final boolean isS2C;
    public OpenItemRegPacket() {
        this.windowId = 0;
        this.isS2C = false;
    }
    public OpenItemRegPacket(int windowId, boolean isS2C) {
        this.windowId = windowId;
        this.isS2C = isS2C;
    }

    public OpenItemRegPacket(FriendlyByteBuf buf) {
        this.windowId = buf.readInt();
        this.isS2C = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.windowId);
        buf.writeBoolean(this.isS2C);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    player.nextContainerCounter();
                    int newWindowId = player.containerCounter;

                    ItemRegMenu menu = new ItemRegMenu(newWindowId, player.getInventory());
                    player.containerMenu = menu;
                    player.initMenu(menu);
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new OpenItemRegPacket(newWindowId, true));
                }
            } else {
                if (this.isS2C) {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientExecutor.run(this.windowId));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientExecutor {
        public static void run(int windowId) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                ItemRegMenu menu = new ItemRegMenu(windowId, mc.player.getInventory());
                mc.player.containerMenu = menu;
                mc.setScreen(new shake1227.trashtreasure.client.ItemRegScreen(menu, mc.player.getInventory(), net.minecraft.network.chat.Component.translatable("gui.trashtreasure.add_item")));
            }
        }
    }
}