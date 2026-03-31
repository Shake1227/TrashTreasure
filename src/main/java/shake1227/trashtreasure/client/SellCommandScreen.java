package shake1227.trashtreasure.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import shake1227.trashtreasure.PacketHandler;
import shake1227.trashtreasure.SyncDataPacket;
import shake1227.trashtreasure.TrashDataManager;

public class SellCommandScreen extends Screen {
    private EditBox commandBox;
    private int page = 0;
    private final int itemsPerPage = 4;

    public SellCommandScreen() {
        super(Component.translatable("gui.trashtreasure.sell_command"));
    }

    @Override
    protected void init() {
        super.init();
        int startY = 45;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, TrashDataManager.sellCommands.size());

        for (int i = startIndex; i < endIndex; i++) {
            final int index = i;
            String cmd = TrashDataManager.sellCommands.get(i);
            String displayCmd = cmd.length() > 25 ? cmd.substring(0, 22) + "..." : cmd;

            this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.delete").append(": ").append(displayCmd), button -> {
                this.minecraft.setScreen(new ConfirmScreen(confirm -> {
                    if (confirm) {
                        TrashDataManager.sellCommands.remove(index);
                        if (this.minecraft.player != null) {
                            this.minecraft.player.sendSystemMessage(Component.translatable("message.trashtreasure.sell_cmd_deleted", cmd).withStyle(ChatFormatting.RED));
                        }
                        saveAndSync();
                    } else {
                        this.minecraft.setScreen(this);
                    }
                }, Component.translatable("gui.trashtreasure.confirm_delete"), Component.literal(cmd)));
            }).bounds(this.width / 2 - 120, startY, 240, 20).build());
            startY += 24;
        }

        if (page > 0) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.prev_page"), button -> {
                page--;
                this.rebuildWidgets();
            }).bounds(this.width / 2 - 120, 145, 115, 20).build());
        }
        if (endIndex < TrashDataManager.sellCommands.size()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.next_page"), button -> {
                page++;
                this.rebuildWidgets();
            }).bounds(this.width / 2 + 5, 145, 115, 20).build());
        }

        this.commandBox = new EditBox(this.font, this.width / 2 - 120, 175, 190, 20, Component.literal(""));
        this.commandBox.setMaxLength(256);
        this.addRenderableWidget(this.commandBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.add"), button -> {
            if (!this.commandBox.getValue().isEmpty()) {
                String newCmd = this.commandBox.getValue();
                TrashDataManager.sellCommands.add(newCmd);
                if (this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(Component.translatable("message.trashtreasure.sell_cmd_added", newCmd).withStyle(ChatFormatting.GREEN));
                }
                saveAndSync();
            }
        }).bounds(this.width / 2 + 75, 175, 45, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.trashtreasure.back"), button -> {
            this.minecraft.setScreen(new MainScreen());
        }).bounds(this.width / 2 - 120, this.height - 30, 240, 20).build());
    }

    private void saveAndSync() {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        PacketHandler.INSTANCE.sendToServer(new SyncDataPacket(TrashDataManager.moneyCommand, gson.toJson(TrashDataManager.sellCommands), gson.toJson(TrashDataManager.registeredItems)));
        this.minecraft.setScreen(new SellCommandScreen());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.trashtreasure.variables_info"), this.width / 2, 25, 0xAAAAAA);
    }
}