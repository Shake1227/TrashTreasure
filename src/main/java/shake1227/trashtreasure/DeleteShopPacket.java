package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class DeleteShopPacket {
    private final int entityId;

    public DeleteShopPacket(int entityId) {
        this.entityId = entityId;
    }

    public DeleteShopPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(this.entityId);
                if (entity instanceof Villager villager) {
                    if (villager.getTags().contains("TrashtreasureShop") || villager.getTags().contains("TrashtreasureAppraiser")) {
                        villager.discard();
                        NotificationUtil.send(player, "SYSTEM", net.minecraft.network.chat.Component.literal("§cショップ/鑑定士村人を削除しました。"));
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}