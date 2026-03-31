package shake1227.trashtreasure.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import shake1227.trashtreasure.PacketHandler;
import shake1227.trashtreasure.SyncAppraiseSettingsPacket;
import shake1227.trashtreasure.TrashDataManager;

public class MinusCommandScreen extends Screen {
    private EditBox commandBox;

    public MinusCommandScreen() {
        super(Component.translatable("gui.trashtreasure.minus_command"));
    }

    @Override
    protected void init() {
        super.init();
        this.commandBox = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 10, 200, 20, Component.literal(""));
        this.commandBox.setMaxLength(256);
        this.commandBox.setValue(TrashDataManager.minusMoneyCommand);
        this.addRenderableWidget(this.commandBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.save_back"), button -> {
            TrashDataManager.minusMoneyCommand = this.commandBox.getValue();
            com.google.gson.Gson gson = new com.google.gson.Gson();
            PacketHandler.INSTANCE.sendToServer(new SyncAppraiseSettingsPacket(TrashDataManager.minAppraiseTime, TrashDataManager.maxAppraiseTime, gson.toJson(TrashDataManager.finishCommands), TrashDataManager.minusMoneyCommand));
            this.minecraft.setScreen(new MainScreen());
        }).bounds(this.width / 2 - 50, this.height / 2 + 20, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.trashtreasure.variables_info"), this.width / 2, this.height / 2 - 25, 0xAAAAAA);
    }
}