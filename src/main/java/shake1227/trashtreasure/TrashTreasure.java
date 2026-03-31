package shake1227.trashtreasure;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import shake1227.trashtreasure.client.ItemRegScreen;

@Mod(TrashTreasure.MODID)
public class TrashTreasure {
    public static final String MODID = "trashtreasure";

    public TrashTreasure() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onClientSetup);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.register();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TrashDataManager.load();
            MenuScreens.register(ModMenuTypes.ITEM_REG_MENU.get(), ItemRegScreen::new);
            MenuScreens.register(ModMenuTypes.APPRAISE_MENU.get(), shake1227.trashtreasure.client.AppraiseScreen::new);
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        TrashDataManager.load();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        TrashDataManager.save();
    }
}