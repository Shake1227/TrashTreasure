package shake1227.trashtreasure;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TrashTreasure.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;
        CompoundTag data = player.getPersistentData();

        if (data.contains("TrashAppraiseItems") && data.contains("TrashAppraiseTicks")) {
            int ticks = data.getInt("TrashAppraiseTicks");
            if (ticks > 0) {
                ticks--;
                data.putInt("TrashAppraiseTicks", ticks);
            }
            if (ticks <= 0) {
                processAppraisalFinish(player, data);
            }
        }
    }

    private static void processAppraisalFinish(ServerPlayer player, CompoundTag data) {
        ListTag list = data.getList("TrashAppraiseItems", 10);
        List<ItemStack> itemsToReturn = new ArrayList<>();
        int totalEarned = 0;

        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.of(list.getCompound(i));
            if (stack.isEmpty()) continue;

            TrashDataManager.TrashItemData match = null;
            String regName = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            for (TrashDataManager.TrashItemData itemData : TrashDataManager.registeredItems) {
                if (regName.equals(itemData.itemRegName)) {
                    match = itemData;
                    break;
                }
            }

            int price = 0;
            if (match != null) {
                if (match.nbtString != null && !match.nbtString.isEmpty()) {
                    try {
                        CompoundTag nbt = net.minecraft.nbt.TagParser.parseTag(match.nbtString);
                        stack.setTag(nbt);
                    } catch (Exception e) {}
                }

                int min = Math.min(match.minPrice, match.maxPrice);
                int max = Math.max(match.minPrice, match.maxPrice);
                price = player.getRandom().nextInt((max - min) + 1) + min;
            }

            totalEarned += price * stack.getCount();
            String priceStr = String.valueOf(price);

            if (stack.hasCustomHoverName()) {
                String nameStr = stack.getHoverName().getString();
                if (nameStr.contains("???") || nameStr.contains("？？？") || nameStr.contains("%price%") || nameStr.contains("%money%")) {
                    nameStr = nameStr.replace("???", priceStr).replace("？？？", priceStr).replace("%price%", priceStr).replace("%money%", priceStr);
                    stack.setHoverName(Component.literal(nameStr));
                }
            }

            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("display", 10)) {
                CompoundTag display = tag.getCompound("display");
                if (display.contains("Lore", 9)) {
                    ListTag lore = display.getList("Lore", 8);
                    ListTag newLore = new ListTag();
                    for (int j = 0; j < lore.size(); j++) {
                        String line = lore.getString(j);
                        line = line.replace("???", priceStr).replace("？？？", priceStr).replace("%price%", priceStr).replace("%money%", priceStr);
                        newLore.add(net.minecraft.nbt.StringTag.valueOf(line));
                    }
                    display.put("Lore", newLore);
                }
            }
            itemsToReturn.add(stack);
        }

        data.remove("TrashAppraiseItems");
        data.remove("TrashAppraiseTicks");
        data.remove("TrashAppraiseTotalTicks");

        for (ItemStack stack : itemsToReturn) {
            net.minecraftforge.items.ItemHandlerHelper.giveItemToPlayer(player, stack);
        }

        String cmd;
        int finalAmount = Math.abs(totalEarned);
        if (totalEarned < 0) {
            cmd = TrashDataManager.minusMoneyCommand;
        } else {
            cmd = TrashDataManager.moneyCommand;
        }

        if (cmd != null && !cmd.isEmpty()) {
            String finalCmd = cmd.replace("%price%", String.valueOf(finalAmount))
                    .replace("%money%", String.valueOf(finalAmount))
                    .replace("%player%", player.getScoreboardName());
            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4), finalCmd);
        }

        for (String finishCmd : TrashDataManager.finishCommands) {
            if (finishCmd != null && !finishCmd.isEmpty()) {
                String finalFinishCmd = finishCmd.replace("%player%", player.getScoreboardName());
                player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4), finalFinishCmd);
            }
        }

        NotificationUtil.send(player, "SUCCESS", Component.translatable("message.trashtreasure.appraise_finish").withStyle(ChatFormatting.GREEN));
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new AppraiseFinishAnimPacket());
        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new SyncProgressPacket(-1, -1));
    }
}