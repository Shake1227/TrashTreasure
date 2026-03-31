package shake1227.trashtreasure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class OpenItemEditPacket {
    private final int index;
    private final int windowId;
    private final boolean isS2C;

    public OpenItemEditPacket(int index) {
        this.index = index;
        this.windowId = 0;
        this.isS2C = false;
    }

    public OpenItemEditPacket(int index, int windowId, boolean isS2C) {
        this.index = index;
        this.windowId = windowId;
        this.isS2C = isS2C;
    }

    public OpenItemEditPacket(FriendlyByteBuf buf) {
        this.index = buf.readInt();
        this.windowId = buf.readInt();
        this.isS2C = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.index);
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
                    menu.editIndex = this.index;
                    player.containerMenu = menu;
                    player.initMenu(menu);

                    if (this.index >= 0 && this.index < TrashDataManager.registeredItems.size()) {
                        TrashDataManager.TrashItemData data = TrashDataManager.registeredItems.get(this.index);
                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data.itemRegName));
                        if (item != null) {
                            ItemStack stack = new ItemStack(item);
                            if (data.nbtString != null && !data.nbtString.isEmpty()) {
                                try {
                                    CompoundTag tag = TagParser.parseTag(data.nbtString);
                                    stack.setTag(tag);
                                } catch (Exception e) {}
                            }
                            try {
                                menu.getSlot(0).set(stack);
                            } catch (Exception e) {}
                        }
                    }

                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new OpenItemEditPacket(this.index, newWindowId, true));
                }
            } else {
                if (this.isS2C) {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientExecutor.run(this.index, this.windowId));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientExecutor {
        public static void run(int index, int windowId) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                ItemRegMenu menu = new ItemRegMenu(windowId, mc.player.getInventory());
                menu.editIndex = index;
                mc.player.containerMenu = menu;
                mc.setScreen(new shake1227.trashtreasure.client.ItemRegScreen(menu, mc.player.getInventory(), net.minecraft.network.chat.Component.translatable("gui.trashtreasure.edit_item")));
            }
        }
    }
}