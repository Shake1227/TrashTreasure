package shake1227.trashtreasure.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import shake1227.trashtreasure.PacketHandler;
import shake1227.trashtreasure.SyncAppraiseSettingsPacket;
import shake1227.trashtreasure.TrashDataManager;

public class AppraiseTimeScreen extends Screen {
    private EditBox minBox, maxBox;

    public AppraiseTimeScreen() { super(Component.translatable("gui.trashtreasure.appraise_time")); }

    @Override
    protected void init() {
        super.init();
        this.minBox = new EditBox(this.font, this.width / 2 - 50, this.height / 2 - 25, 100, 20, Component.empty());
        this.minBox.setValue(String.valueOf(TrashDataManager.minAppraiseTime));
        this.maxBox = new EditBox(this.font, this.width / 2 - 50, this.height / 2 + 5, 100, 20, Component.empty());
        this.maxBox.setValue(String.valueOf(TrashDataManager.maxAppraiseTime));
        this.addRenderableWidget(this.minBox);
        this.addRenderableWidget(this.maxBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.save_back"), b -> {
            try {
                int min = Integer.parseInt(this.minBox.getValue());
                int max = Integer.parseInt(this.maxBox.getValue());
                PacketHandler.INSTANCE.sendToServer(new SyncAppraiseSettingsPacket(min, max, new com.google.gson.Gson().toJson(TrashDataManager.finishCommands), TrashDataManager.minusMoneyCommand));
                this.minecraft.setScreen(new MainScreen());
            } catch (NumberFormatException ignored) {}
        }).bounds(this.width / 2 - 50, this.height / 2 + 35, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics gg, int x, int y, float pt) {
        this.renderBackground(gg);
        super.render(gg, x, y, pt);
        gg.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        gg.drawString(this.font, "Min(Tick):", this.width / 2 - 110, this.height / 2 - 19, 0xFFFFFF);
        gg.drawString(this.font, "Max(Tick):", this.width / 2 - 110, this.height / 2 + 11, 0xFFFFFF);
        gg.drawCenteredString(this.font, "※20Tick = 1秒", this.width / 2, this.height / 2 - 45, 0xAAAAAA);
    }
}