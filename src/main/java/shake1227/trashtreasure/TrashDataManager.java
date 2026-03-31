package shake1227.trashtreasure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TrashDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SAVE_PATH = FMLPaths.CONFIGDIR.get().resolve("trashtreasure_data.json").toAbsolutePath();

    public static int minAppraiseTime = 60;
    public static int maxAppraiseTime = 120;
    public static List<String> finishCommands = new ArrayList<>();
    public static String minusMoneyCommand = "";
    public static String moneyCommand = "";
    public static List<String> sellCommands = new ArrayList<>();
    public static List<TrashItemData> registeredItems = new ArrayList<>();

    static {
        load();
    }

    public static void load() {
        try {
            if (Files.exists(SAVE_PATH)) {
                try (Reader reader = Files.newBufferedReader(SAVE_PATH)) {
                    SaveData data = GSON.fromJson(reader, SaveData.class);
                    if (data != null) {
                        minAppraiseTime = data.minAppraiseTime;
                        maxAppraiseTime = data.maxAppraiseTime;
                        finishCommands = data.finishCommands != null ? data.finishCommands : new ArrayList<>();
                        minusMoneyCommand = data.minusMoneyCommand != null ? data.minusMoneyCommand : "";
                        moneyCommand = data.moneyCommand != null ? data.moneyCommand : "";
                        sellCommands = data.sellCommands != null ? data.sellCommands : new ArrayList<>();
                        registeredItems = data.registeredItems != null ? data.registeredItems : new ArrayList<>();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            SaveData data = new SaveData();
            data.minAppraiseTime = minAppraiseTime;
            data.maxAppraiseTime = maxAppraiseTime;
            data.finishCommands = finishCommands;
            data.minusMoneyCommand = minusMoneyCommand;
            data.moneyCommand = moneyCommand;
            data.sellCommands = sellCommands;
            data.registeredItems = registeredItems;

            try (Writer writer = Files.newBufferedWriter(SAVE_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Component getItemDisplayName(TrashItemData data) {
        if (data == null || data.itemRegName == null || data.itemRegName.isEmpty()) {
            return Component.literal("Unknown");
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data.itemRegName));
        if (item != null) {
            ItemStack stack = new ItemStack(item);
            if (data.nbtString != null && !data.nbtString.isEmpty()) {
                try {
                    stack.setTag(TagParser.parseTag(data.nbtString));
                } catch (Exception e) {}
            }
            return stack.getHoverName();
        }
        return Component.literal(data.itemRegName);
    }

    public static class SaveData {
        public int minAppraiseTime = 60;
        public int maxAppraiseTime = 120;
        public List<String> finishCommands = new ArrayList<>();
        public String minusMoneyCommand = "";
        public String moneyCommand = "";
        public List<String> sellCommands = new ArrayList<>();
        public List<TrashItemData> registeredItems = new ArrayList<>();
    }

    public static class TrashItemData {
        public String itemRegName = "";
        public String nbtString = "";
        public double dropChance = 0.0;
        public int minPrice = 0;
        public int maxPrice = 0;

        public TrashItemData() {}

        public TrashItemData(String itemRegName, String nbtString, double dropChance, int minPrice, int maxPrice) {
            this.itemRegName = itemRegName;
            this.nbtString = nbtString;
            this.dropChance = dropChance;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
}