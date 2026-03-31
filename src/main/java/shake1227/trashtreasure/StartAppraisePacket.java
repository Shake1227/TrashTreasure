package shake1227.trashtreasure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class StartAppraisePacket {
    public StartAppraisePacket() {}
    public StartAppraisePacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu != null) {
                CompoundTag data = player.getPersistentData();
                ListTag list = data.contains("TrashAppraiseItems") ? data.getList("TrashAppraiseItems", 10) : new ListTag();
                boolean added = false;

                for (int i = 0; i < player.containerMenu.slots.size(); i++) {
                    net.minecraft.world.inventory.Slot slot = player.containerMenu.slots.get(i);
                    if (slot.container != player.getInventory()) {
                        ItemStack stack = slot.getItem();
                        if (!stack.isEmpty()) {
                            list.add(stack.copy().save(new CompoundTag()));
                            slot.set(ItemStack.EMPTY);
                            added = true;
                        }
                    }
                }

                if (added) {
                    data.put("TrashAppraiseItems", list);
                    if (!data.contains("TrashAppraiseTicks") || data.getInt("TrashAppraiseTicks") <= 0) {
                        int min = TrashDataManager.minAppraiseTime;
                        int max = TrashDataManager.maxAppraiseTime;
                        int time = min;
                        if (max > min) {
                            time = player.getRandom().nextInt((max - min) + 1) + min;
                        }
                        int ticks = time * 20;
                        data.putInt("TrashAppraiseTicks", ticks);
                        data.putInt("TrashAppraiseTotalTicks", ticks);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}