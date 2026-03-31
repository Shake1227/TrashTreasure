package shake1227.trashtreasure.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shake1227.trashtreasure.ItemRegMenu;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

    public static void openMainGui() {
        Minecraft.getInstance().setScreen(new MainScreen());
    }

    public static void openItemRegGui() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().setScreen(new ItemRegScreen(new ItemRegMenu(0, Minecraft.getInstance().player.getInventory()), Minecraft.getInstance().player.getInventory(), Component.translatable("gui.trashtreasure.add_item")));
        }
    }

    public static void openItemEditGui(int index) {
        if (Minecraft.getInstance().player != null) {
            ItemRegMenu menu = new ItemRegMenu(0, Minecraft.getInstance().player.getInventory());
            menu.editIndex = index;
            Minecraft.getInstance().setScreen(new ItemRegScreen(menu, Minecraft.getInstance().player.getInventory(), Component.translatable("gui.trashtreasure.edit_item")));
        }
    }

    public static void openShopDeleteGui(int entityId) {
        Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(confirm -> {
            if (confirm) {
                shake1227.trashtreasure.PacketHandler.INSTANCE.sendToServer(new shake1227.trashtreasure.DeleteShopPacket(entityId));
            }
            Minecraft.getInstance().setScreen(null);
        }, Component.translatable("gui.trashtreasure.confirm_delete_shop"), Component.empty()));
    }

    public static void openInterruptGui() {
        Minecraft.getInstance().setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(confirm -> {
            if (confirm) {
                shake1227.trashtreasure.PacketHandler.INSTANCE.sendToServer(new shake1227.trashtreasure.CancelAppraisePacket());
            }
            Minecraft.getInstance().setScreen(null);
        }, Component.translatable("gui.trashtreasure.confirm_interrupt"), Component.empty()));
    }

    public static void handleSyncProgress(int current, int total) {
        ClientHudEvents.currentTicks = current;
        if (total > 0) ClientHudEvents.totalTicks = total;
        if (current == total && total > 0) {
            ClientHudEvents.startAppraisal();
        } else if (current > 0) {
            if (ClientHudEvents.state == 0) ClientHudEvents.startAppraisal();
        } else if (current < 0) {
            ClientHudEvents.cancelAppraisal();
        }
    }

    public static void handleAppraiseFinishAnim() {
        ClientHudEvents.triggerFinishAnimation();
    }

    public static void handleShowNotification(String category, String componentJson) {
        if (net.minecraftforge.fml.ModList.get().isLoaded("modernnotification")) {
            Component message = Component.Serializer.fromJson(componentJson);
            shake1227.trashtreasure.compat.ModernNotificationCompat.showNotification(category, message);
        }
    }
}