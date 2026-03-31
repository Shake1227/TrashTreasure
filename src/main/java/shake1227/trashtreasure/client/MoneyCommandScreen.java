package shake1227.trashtreasure.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import shake1227.trashtreasure.PacketHandler;
import shake1227.trashtreasure.SyncDataPacket;
import shake1227.trashtreasure.TrashDataManager;

public class MoneyCommandScreen extends Screen {
    private EditBox commandBox;

    public MoneyCommandScreen() {
        super(Component.translatable("gui.trashtreasure.money_command"));
    }

    @Override
    protected void init() {
        super.init();
        this.commandBox = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 10, 200, 20, Component.literal(""));
        this.commandBox.setMaxLength(256);
        this.commandBox.setValue(TrashDataManager.moneyCommand);
        this.addRenderableWidget(this.commandBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.save_back"), button -> {
            TrashDataManager.moneyCommand = this.commandBox.getValue();
            com.google.gson.Gson gson = new com.google.gson.Gson();
            PacketHandler.INSTANCE.sendToServer(new SyncDataPacket(TrashDataManager.moneyCommand, gson.toJson(TrashDataManager.sellCommands), gson.toJson(TrashDataManager.registeredItems)));

            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.translatable("message.trashtreasure.money_saved").withStyle(ChatFormatting.GREEN));
            }
            this.minecraft.setScreen(new MainScreen());
        }).bounds(this.width / 2 - 50, this.height / 2 + 20, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.trashtreasure.money_example"), this.width / 2, this.height / 2 - 25, 0xAAAAAA);
    }
}