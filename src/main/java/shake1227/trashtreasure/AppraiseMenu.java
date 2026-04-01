package shake1227.trashtreasure;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class AppraiseMenu extends AbstractContainerMenu {
    public final Player player;
    public final SimpleContainer appraiseContainer;

    public int progress = 0;
    public int maxProgress = 0;
    public boolean isAppraising = false;
    public int appraiseTime = 0;

    public AppraiseMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv);
    }

    public AppraiseMenu(int id, Inventory inv) {
        super(ModMenuTypes.APPRAISE_MENU.get(), id);
        this.player = inv.player;

        this.appraiseContainer = new SimpleContainer(54) {
            @Override
            public void setChanged() {
                super.setChanged();
                AppraiseMenu.this.slotsChanged(this);
            }
        };

        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(this.appraiseContainer, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 139 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 197));
        }
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.appraiseContainer.stillValid(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int containerSize = this.appraiseContainer.getContainerSize();

            if (index < containerSize) {
                if (!this.moveItemStackTo(itemstack1, containerSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, containerSize, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public void finishAppraising() {
        processAndReturnItems(true);
    }

    public void cancelAppraising() {
        processAndReturnItems(false);
    }

    private void processAndReturnItems(boolean isFinished) {
        for (int i = 0; i < appraiseContainer.getContainerSize(); i++) {
            ItemStack stack = appraiseContainer.getItem(i);
            if (!stack.isEmpty()) {
                ItemStack returnStack = stack.copy();
                appraiseContainer.setItem(i, ItemStack.EMPTY);

                if (isFinished) {
                    applyAppraisalLore(returnStack);
                }

                if (!this.player.getInventory().add(returnStack)) {
                    this.player.drop(returnStack, false);
                }
            }
        }
    }

    private void applyAppraisalLore(ItemStack stack) {
        var res = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (res == null) return;
        String regName = res.toString();

        TrashDataManager.TrashItemData data = null;
        for (TrashDataManager.TrashItemData d : TrashDataManager.registeredItems) {
            if (d.itemRegName.equals(regName)) {
                data = d;
                break;
            }
        }

        if (data != null) {
            int price = data.minPrice + new Random().nextInt(Math.max(1, data.maxPrice - data.minPrice + 1));
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag display = stack.getOrCreateTagElement("display");

            if (display.contains("Lore", 9)) {
                ListTag lore = display.getList("Lore", 8);
                ListTag newLore = new ListTag();

                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.getString(i);
                    if (line.contains("???")) {
                        line = line.replace("???", String.format("%,d", price));
                    }
                    newLore.add(StringTag.valueOf(line));
                }
                display.put("Lore", newLore);
            }
        }
    }
}