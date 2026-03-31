package shake1227.trashtreasure;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;

public class NotificationUtil {
    public static void send(Player player, String category, Component message) {
        if (ModList.get().isLoaded("modernnotification")) {
            if (player.level().isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    shake1227.trashtreasure.compat.ModernNotificationCompat.showNotification(category, message);
                });
            } else if (player instanceof ServerPlayer sp) {
                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                        new ShowNotificationPacket(category, Component.Serializer.toJson(message)));
            }
        } else {
            player.sendSystemMessage(message);
        }
    }
}