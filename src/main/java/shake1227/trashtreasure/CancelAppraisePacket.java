package shake1227.trashtreasure;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class CancelAppraisePacket {
    public CancelAppraisePacket() {}
    public CancelAppraisePacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                CompoundTag data = player.getPersistentData();
                if (data.contains("TrashAppraiseItems")) {
                    ListTag list = data.getList("TrashAppraiseItems", 10);
                    // リストの内容をすべて走査して確実に返却
                    for (int i = 0; i < list.size(); i++) {
                        ItemStack stack = ItemStack.of(list.getCompound(i));
                        if (!stack.isEmpty()) {
                            if (!player.getInventory().add(stack)) {
                                player.drop(stack, false);
                            }
                        }
                    }
                    // 返却後にデータを一括削除
                    data.remove("TrashAppraiseItems");
                    data.remove("TrashAppraiseTicks");
                    data.remove("TrashAppraiseTotalTicks");

                    NotificationUtil.send(player, "FAILURE", Component.translatable("message.trashtreasure.appraise_cancel").withStyle(ChatFormatting.RED));
                    PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new SyncProgressPacket(-1, -1));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}