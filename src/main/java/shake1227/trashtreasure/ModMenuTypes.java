package shake1227.trashtreasure;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TrashTreasure.MODID);

    public static final RegistryObject<MenuType<ItemRegMenu>> ITEM_REG_MENU = MENUS.register("item_reg_menu", () -> IForgeMenuType.create(ItemRegMenu::new));
    public static final RegistryObject<MenuType<AppraiseMenu>> APPRAISE_MENU = MENUS.register("appraise_menu", () -> IForgeMenuType.create(AppraiseMenu::new));
}