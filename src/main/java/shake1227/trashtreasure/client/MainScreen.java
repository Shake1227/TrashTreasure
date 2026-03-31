package shake1227.trashtreasure.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MainScreen extends Screen {

    public MainScreen() {
        super(Component.translatable("gui.trashtreasure.main_title"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.manage_items"), b -> this.minecraft.setScreen(new ItemManageScreen())).bounds(centerX - 155, centerY - 40, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.money_command"), b -> this.minecraft.setScreen(new MoneyCommandScreen())).bounds(centerX - 155, centerY - 10, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.sell_command"), b -> this.minecraft.setScreen(new SellCommandScreen())).bounds(centerX - 155, centerY + 20, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.appraise_time"), b -> this.minecraft.setScreen(new AppraiseTimeScreen())).bounds(centerX + 5, centerY - 40, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.finish_command"), b -> this.minecraft.setScreen(new FinishCommandScreen())).bounds(centerX + 5, centerY - 10, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.minus_command"), b -> this.minecraft.setScreen(new MinusCommandScreen())).bounds(centerX + 5, centerY + 20, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}