package shake1227.trashtreasure.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shake1227.trashtreasure.ItemRegMenu;
import shake1227.trashtreasure.NotificationUtil;
import shake1227.trashtreasure.PacketHandler;
import shake1227.trashtreasure.SyncDataPacket;
import shake1227.trashtreasure.TrashDataManager;

public class ItemRegScreen extends AbstractContainerScreen<ItemRegMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/dispenser.png");
    private EditBox chanceBox;
    private EditBox minPriceBox;
    private EditBox maxPriceBox;

    public ItemRegScreen(ItemRegMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        boolean isEdit = this.menu.editIndex != -1;

        this.chanceBox = new EditBox(this.font, this.leftPos + 105, this.topPos + 18, 55, 14, Component.literal(""));
        this.minPriceBox = new EditBox(this.font, this.leftPos + 105, this.topPos + 38, 55, 14, Component.literal(""));
        this.maxPriceBox = new EditBox(this.font, this.leftPos + 105, this.topPos + 58, 55, 14, Component.literal(""));

        if (isEdit) {
            TrashDataManager.TrashItemData data = TrashDataManager.registeredItems.get(this.menu.editIndex);
            this.chanceBox.setValue(String.valueOf(data.dropChance));
            this.minPriceBox.setValue(String.valueOf(data.minPrice));
            this.maxPriceBox.setValue(String.valueOf(data.maxPrice));
        } else {
            this.chanceBox.setValue("1.0");
            this.minPriceBox.setValue("100");
            this.maxPriceBox.setValue("500");
        }

        this.addRenderableWidget(this.chanceBox);
        this.addRenderableWidget(this.minPriceBox);
        this.addRenderableWidget(this.maxPriceBox);

        Component primaryBtnText = isEdit ? Component.translatable("gui.trashtreasure.save") : Component.translatable("gui.trashtreasure.add");

        this.addRenderableWidget(Button.builder(primaryBtnText, button -> {
            ItemStack stack = this.menu.getRegisteredItem();
            if (!stack.isEmpty()) {
                try {
                    String regName = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                    double chance = Double.parseDouble(this.chanceBox.getValue());
                    int min = Integer.parseInt(this.minPriceBox.getValue());
                    int max = Integer.parseInt(this.maxPriceBox.getValue());
                    String nbt = stack.hasTag() ? stack.getTag().toString() : "";

                    TrashDataManager.TrashItemData newData = new TrashDataManager.TrashItemData(regName, nbt, chance, min, max);

                    if (isEdit) {
                        TrashDataManager.registeredItems.set(this.menu.editIndex, newData);
                    } else {
                        TrashDataManager.registeredItems.add(newData);
                    }

                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    PacketHandler.INSTANCE.sendToServer(new SyncDataPacket(TrashDataManager.moneyCommand, gson.toJson(TrashDataManager.sellCommands), gson.toJson(TrashDataManager.registeredItems)));

                    if (this.minecraft != null && this.minecraft.player != null) {
                        Component itemName = TrashDataManager.getItemDisplayName(newData);
                        String langKey = isEdit ? "message.trashtreasure.item_updated" : "message.trashtreasure.item_added";
                        NotificationUtil.send(this.minecraft.player, "SUCCESS", Component.translatable(langKey, Component.empty().append(itemName).append("§r")));
                        this.minecraft.player.closeContainer();
                    }
                } catch (NumberFormatException ignored) {}
            }
        }).bounds(isEdit ? this.leftPos + 7 : this.leftPos + 12, this.topPos + 48, isEdit ? 35 : 45, 20).build());

        if (isEdit) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.delete"), button -> {
                this.minecraft.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(confirm -> {
                    if (confirm) {
                        TrashDataManager.TrashItemData data = TrashDataManager.registeredItems.get(this.menu.editIndex);
                        Component itemName = TrashDataManager.getItemDisplayName(data);
                        TrashDataManager.registeredItems.remove(this.menu.editIndex);

                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        PacketHandler.INSTANCE.sendToServer(new SyncDataPacket(TrashDataManager.moneyCommand, gson.toJson(TrashDataManager.sellCommands), gson.toJson(TrashDataManager.registeredItems)));

                        if (this.minecraft.player != null) {
                            NotificationUtil.send(this.minecraft.player, "FAILURE", Component.translatable("message.trashtreasure.item_deleted", Component.empty().append(itemName).append("§r")));
                        }
                        this.minecraft.player.closeContainer();
                    } else {
                        this.minecraft.setScreen(this);
                    }
                }, Component.translatable("gui.trashtreasure.confirm_delete"), TrashDataManager.getItemDisplayName(TrashDataManager.registeredItems.get(this.menu.editIndex))));
            }).bounds(this.leftPos + 44, this.topPos + 48, 35, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(this.font, Component.translatable("gui.trashtreasure.chance"), this.leftPos + 84, this.topPos + 21, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("gui.trashtreasure.min"), this.leftPos + 84, this.topPos + 41, 0xFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("gui.trashtreasure.max"), this.leftPos + 84, this.topPos + 61, 0xFFFFFF);

        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.trashtreasure.item_label"), this.leftPos + 34, this.topPos + 14, 0xAAAAAA);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.fill(this.leftPos + 7, this.topPos + 7, this.leftPos + 169, this.topPos + 72, 0xFFC6C6C6);
        int slotX = this.leftPos + 25;
        int slotY = this.topPos + 25;
        guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
        guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF8B8B8B);
    }
}