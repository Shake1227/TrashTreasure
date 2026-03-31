package shake1227.trashtreasure.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import shake1227.trashtreasure.OpenItemEditPacket;
import shake1227.trashtreasure.OpenItemRegPacket;
import shake1227.trashtreasure.PacketHandler;
import shake1227.trashtreasure.TrashDataManager;

public class ItemManageScreen extends Screen {
    private int page = 0;
    private final int itemsPerPage = 5;

    public ItemManageScreen() {
        super(Component.translatable("gui.trashtreasure.manage_items"));
    }

    @Override
    protected void init() {
        super.init();
        int y = 40;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, TrashDataManager.registeredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            final int index = i;
            TrashDataManager.TrashItemData data = TrashDataManager.registeredItems.get(i);

            Component displayName = TrashDataManager.getItemDisplayName(data);
            Component buttonText = Component.empty().append(displayName).append("§r");

            this.addRenderableWidget(Button.builder(buttonText, button -> {
                PacketHandler.INSTANCE.sendToServer(new OpenItemEditPacket(index));
            }).bounds(this.width / 2 - 100, y, 200, 20).build());
            y += 24;
        }

        if (page > 0) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.prev_page"), button -> {
                page--;
                this.rebuildWidgets();
            }).bounds(this.width / 2 - 100, 165, 95, 20).build());
        }
        if (endIndex < TrashDataManager.registeredItems.size()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.next_page"), button -> {
                page++;
                this.rebuildWidgets();
            }).bounds(this.width / 2 + 5, 165, 95, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.add_item"), button -> {
            PacketHandler.INSTANCE.sendToServer(new OpenItemRegPacket());
        }).bounds(this.width / 2 - 100, this.height - 55, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.back"), button -> {
            this.minecraft.setScreen(new MainScreen());
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}