package shake1227.trashtreasure.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import shake1227.trashtreasure.AppraiseMenu;

public class AppraiseScreen extends AbstractContainerScreen<AppraiseMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public AppraiseScreen(AppraiseMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 167;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);
        super.render(gg, mouseX, mouseY, partialTick);
        this.renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float pt, int x, int y) {
        gg.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, 71);
        gg.blit(TEXTURE, this.leftPos, this.topPos + 71, 0, 126, this.imageWidth, 96);
    }
}