package shake1227.trashtreasure;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TrashTreasure.MODID);

    public static final RegistryObject<Item> JUNK_PILE_ITEM = ITEMS.register("junk_pile",
            () -> new BlockItem(ModBlocks.JUNK_PILE.get(), new Item.Properties()));
}