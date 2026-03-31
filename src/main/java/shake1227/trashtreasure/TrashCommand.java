package shake1227.trashtreasure;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.ForgeRegistries;

public class TrashCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("trashtreasure")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("open")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new OpenGuiPacket());
                            return 1;
                        })
                )
                .then(Commands.literal("shop")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            net.minecraft.world.entity.npc.Villager villager = net.minecraft.world.entity.EntityType.VILLAGER.create(player.level());
                            if (villager != null) {
                                villager.setPos(player.getX(), player.getY(), player.getZ());
                                villager.setNoAi(true);
                                villager.setInvulnerable(true);
                                villager.getTags().add("TrashtreasureShop");
                                villager.setCustomName(Component.translatable("entity.trashtreasure.shop_villager"));
                                villager.setCustomNameVisible(true);
                                player.level().addFreshEntity(villager);
                                context.getSource().sendSuccess(() -> Component.literal("§aショップ村人を召喚しました！"), true);
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("judgment")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            net.minecraft.world.entity.npc.Villager villager = net.minecraft.world.entity.EntityType.VILLAGER.create(player.level());
                            if (villager != null) {
                                villager.setPos(player.getX(), player.getY(), player.getZ());
                                villager.setNoAi(true);
                                villager.setInvulnerable(true);
                                villager.getTags().add("TrashtreasureAppraiser");
                                villager.setCustomName(Component.translatable("entity.trashtreasure.appraiser_villager"));
                                villager.setCustomNameVisible(true);
                                player.level().addFreshEntity(villager);
                                context.getSource().sendSuccess(() -> Component.literal("§a鑑定士村人を召喚しました！"), true);
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                .then(Commands.argument("chance", DoubleArgumentType.doubleArg(0.01, 100.0))
                                        .then(Commands.argument("minPrice", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("maxPrice", IntegerArgumentType.integer(1))
                                                        .executes(context -> {
                                                            String regName = ForgeRegistries.ITEMS.getKey(ItemArgument.getItem(context, "item").getItem()).toString();
                                                            double chance = DoubleArgumentType.getDouble(context, "chance");
                                                            int min = IntegerArgumentType.getInteger(context, "minPrice");
                                                            int max = IntegerArgumentType.getInteger(context, "maxPrice");

                                                            TrashDataManager.registeredItems.add(new TrashDataManager.TrashItemData(regName, "", chance, min, max));
                                                            TrashDataManager.save();
                                                            context.getSource().sendSuccess(() -> Component.literal("§aアイテムを追加しました: " + regName), true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }
}