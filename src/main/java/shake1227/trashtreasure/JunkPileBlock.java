package shake1227.trashtreasure;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class JunkPileBlock extends Block {
    private static final Random RANDOM = new Random();

    public JunkPileBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            dropTrashItem(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void dropTrashItem(Level level, BlockPos pos) {
        if (TrashDataManager.registeredItems.isEmpty()) return;

        double totalWeight = TrashDataManager.registeredItems.stream().mapToDouble(d -> d.dropChance).sum();
        double randomVal = RANDOM.nextDouble() * totalWeight;

        TrashDataManager.TrashItemData selected = null;
        for (TrashDataManager.TrashItemData data : TrashDataManager.registeredItems) {
            randomVal -= data.dropChance;
            if (randomVal <= 0) {
                selected = data;
                break;
            }
        }

        if (selected != null) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(selected.itemRegName));
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                try {
                    if (selected.nbtString != null && !selected.nbtString.isEmpty()) {
                        CompoundTag tag = TagParser.parseTag(selected.nbtString);
                        stack.setTag(tag);
                    }

                    CompoundTag stackTag = stack.getOrCreateTag();

                    int minP = Math.min(selected.minPrice, selected.maxPrice);
                    int maxP = Math.max(selected.minPrice, selected.maxPrice);
                    int price = minP + (maxP > minP ? RANDOM.nextInt(maxP - minP + 1) : 0);

                    stackTag.putInt("TrashPrice", price);
                    stackTag.putBoolean("Unappraised", true);

                    CompoundTag displayTag = stack.getOrCreateTagElement("display");
                    ListTag loreList;
                    if (displayTag.contains("Lore", 9)) {
                        loreList = displayTag.getList("Lore", 8);
                    } else {
                        loreList = new ListTag();
                        displayTag.put("Lore", loreList);
                    }

                    Component loreText = Component.translatable("lore.trashtreasure.unappraised").withStyle(ChatFormatting.GRAY);
                    loreList.add(StringTag.valueOf(Component.Serializer.toJson(loreText)));

                    ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    level.addFreshEntity(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}