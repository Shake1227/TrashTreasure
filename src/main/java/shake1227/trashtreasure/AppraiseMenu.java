package shake1227.trashtreasure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class AppraiseMenu extends AbstractContainerMenu {
    private final ItemStackHandler itemHandler = new ItemStackHandler(27);

    public AppraiseMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv);
    }

    public AppraiseMenu(int id, Inventory inv) {
        super(ModMenuTypes.APPRAISE_MENU.get(), id);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new SlotItemHandler(itemHandler, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inv, k + l * 9 + 9, 8 + k * 18, 84 + l * 18));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inv, i1, 8 + i1 * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(itemstack1, 27, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            List<ItemStack> toAppraise = new ArrayList<>();
            List<ItemStack> toReturn = new ArrayList<>();
            for (int i = 0; i < 27; i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (stack.hasTag() && stack.getTag().getBoolean("Unappraised")) {
                        toAppraise.add(stack);
                    } else {
                        toReturn.add(stack);
                    }
                }
            }
            for (ItemStack stack : toReturn) {
                if (!player.getInventory().add(stack)) player.drop(stack, false);
            }
            if (!toAppraise.isEmpty()) {
                CompoundTag data = player.getPersistentData();
                ListTag list = new ListTag();
                int totalCount = 0;
                for (ItemStack stack : toAppraise) {
                    list.add(stack.save(new CompoundTag()));
                    totalCount += stack.getCount();
                }
                data.put("TrashAppraiseItems", list);

                int min = TrashDataManager.minAppraiseTime;
                int max = TrashDataManager.maxAppraiseTime;
                int timePerItem = min + (max > min ? player.getRandom().nextInt(max - min + 1) : 0);
                int totalTime = timePerItem * totalCount;

                data.putInt("TrashAppraiseTicks", totalTime);
                data.putInt("TrashAppraiseTotalTicks", totalTime);
                NotificationUtil.send(player, "SYSTEM", Component.translatable("message.trashtreasure.appraise_start", totalCount));

                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp), new SyncProgressPacket(totalTime, totalTime));
                }
            }
        }
    }
}