package shake1227.trashtreasure;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = TrashTreasure.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TrashEventHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockPos pos = event.getPos();
        Level level = event.getLevel();

        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            SignText frontText = sign.getFrontText();
            String rawText = ChatFormatting.stripFormatting(frontText.getMessage(0, false).getString());

            if (rawText != null && rawText.trim().equalsIgnoreCase("[selltrash]")) {
                event.setCanceled(true);
                if (!level.isClientSide) {
                    SignText newText = frontText
                            .setMessage(0, Component.translatable("sign.trashtreasure.header"))
                            .setMessage(1, Component.translatable("sign.trashtreasure.click"));
                    sign.setText(newText, true);
                    sign.setChanged();
                    level.sendBlockUpdated(pos, sign.getBlockState(), sign.getBlockState(), 3);
                }
                return;
            }

            if (frontText.getMessage(0, false).getContents() instanceof TranslatableContents translatable) {
                if (translatable.getKey().equals("sign.trashtreasure.header")) {
                    event.setCanceled(true);
                    if (!level.isClientSide && event.getEntity() instanceof ServerPlayer player) {
                        processSell(player);
                    }
                }
            }
        }
    }

    private static void processSell(ServerPlayer player) {
        ItemStack handItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (handItem.isEmpty()) {
            NotificationUtil.send(player, "WARNING", Component.translatable("message.trashtreasure.invalid"));
            return;
        }

        CompoundTag tag = handItem.getTag();
        if (tag != null && tag.contains("TrashPrice")) {
            int pricePerItem = tag.getInt("TrashPrice");
            int amount = handItem.getCount();
            int totalPrice = pricePerItem * amount;

            Component itemName = handItem.hasCustomHoverName() ? handItem.getHoverName() : Component.literal(ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString());
            Component coloredName = Component.empty().append(itemName).append("§r");

            handItem.shrink(amount);

            String moneyCmd = TrashDataManager.moneyCommand.replace("%player%", player.getGameProfile().getName()).replace("%money%", String.valueOf(totalPrice));
            player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack(), moneyCmd);

            for (String cmd : TrashDataManager.sellCommands) {
                String execCmd = cmd.replace("%player%", player.getGameProfile().getName()).replace("%money%", String.valueOf(totalPrice));
                player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack(), execCmd);
            }
            NotificationUtil.send(player, "SUCCESS", Component.translatable("message.trashtreasure.sold", coloredName, totalPrice));
        } else {
            NotificationUtil.send(player, "WARNING", Component.translatable("message.trashtreasure.invalid"));
        }
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        TrashCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && !event.player.level().isClientSide && event.player instanceof ServerPlayer player) {
            CompoundTag data = player.getPersistentData();
            if (data.contains("TrashAppraiseTicks")) {
                int ticks = data.getInt("TrashAppraiseTicks");
                if (ticks > 0) {
                    data.putInt("TrashAppraiseTicks", ticks - 1);
                    if (ticks % 20 == 0) {
                        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new SyncProgressPacket(ticks - 1, data.getInt("TrashAppraiseTotalTicks")));
                    }
                } else if (ticks == 0) {
                    PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new AppraiseFinishAnimPacket());
                    data.putInt("TrashAppraiseTicks", -1);
                } else if (ticks < 0 && ticks >= -16) {
                    data.putInt("TrashAppraiseTicks", ticks - 1);
                } else if (ticks == -17) {
                    data.putInt("TrashAppraiseTicks", -999);
                    finishAppraisal(player, data);
                }
            }
        }
    }

    private static void finishAppraisal(ServerPlayer player, CompoundTag data) {
        net.minecraft.nbt.ListTag list = data.getList("TrashAppraiseItems", 10);
        int totalPrice = 0;

        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.of(list.getCompound(i));
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove("Unappraised");
                int price = tag.getInt("TrashPrice");
                totalPrice += (price * stack.getCount());

                CompoundTag display = stack.getOrCreateTagElement("display");
                net.minecraft.nbt.ListTag lore = new net.minecraft.nbt.ListTag();
                lore.add(net.minecraft.nbt.StringTag.valueOf(Component.Serializer.toJson(Component.translatable("lore.trashtreasure.price", price).withStyle(ChatFormatting.GOLD))));
                display.put("Lore", lore);
            }
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }

        data.remove("TrashAppraiseItems");
        data.remove("TrashAppraiseTicks");
        data.remove("TrashAppraiseTotalTicks");

        if (totalPrice < 0 && !TrashDataManager.minusMoneyCommand.isEmpty()) {
            String cmd = TrashDataManager.minusMoneyCommand.replace("%player%", player.getGameProfile().getName()).replace("%money%", String.valueOf(totalPrice)).replace("&", "§");
            player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack(), cmd);
        }
        for (String cmdTemp : TrashDataManager.finishCommands) {
            String cmd = cmdTemp.replace("%player%", player.getGameProfile().getName()).replace("%money%", String.valueOf(totalPrice)).replace("&", "§");
            player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack(), cmd);
        }

        NotificationUtil.send(player, "SUCCESS", Component.translatable("message.trashtreasure.appraise_finish").withStyle(ChatFormatting.AQUA));
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (event.getTarget() instanceof net.minecraft.world.entity.npc.Villager villager) {
            boolean isShop = villager.getTags().contains("TrashtreasureShop");
            boolean isAppraiser = villager.getTags().contains("TrashtreasureAppraiser");

            if (isShop || isAppraiser) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);

                if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
                    if (player.hasPermissions(2) && player.isShiftKeyDown()) {
                        PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new OpenShopDeletePacket(villager.getId()));
                    } else {
                        if (isShop) {
                            processSell(player);
                        } else if (isAppraiser) {
                            CompoundTag data = player.getPersistentData();
                            if (data.contains("TrashAppraiseTicks") && data.getInt("TrashAppraiseTicks") > 0) {
                                PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new OpenInterruptPacket());
                            } else {
                                net.minecraftforge.network.NetworkHooks.openScreen(player, new net.minecraft.world.MenuProvider() {
                                    @Override public Component getDisplayName() { return Component.translatable("gui.trashtreasure.appraise_menu"); }
                                    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new AppraiseMenu(id, inv); }
                                });
                            }
                        }
                    }
                }
            }
        }
    }
}