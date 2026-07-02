package top.aurora.lordofmysteries.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import top.aurora.lordofmysteries.registry.ModBlockEntities;
import top.aurora.lordofmysteries.registry.ModItems;

public final class CrucibleBlockEntity extends BlockEntity {

    public static final int BREWING_TIME = 1200;
    private final NonNullList<ItemStack> ingredients = NonNullList.withSize(3, ItemStack.EMPTY);
    private ItemStack result = ItemStack.EMPTY;
    private int brewingTicks;
    private float temperature;
    private float accumulatedTemperature;
    private boolean brewing;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public boolean canInsert(ItemStack stack) {
        return !brewing && result.isEmpty() && isIngredient(stack) && firstEmptySlot() >= 0;
    }

    public boolean insert(ItemStack held, Player player) {
        int slot = firstEmptySlot();
        if (slot < 0 || !canInsert(held)) return false;
        ItemStack inserted = held.copy();
        inserted.setCount(1);
        ingredients.set(slot, inserted);
        if (!player.getAbilities().instabuild) held.shrink(1);
        setChanged();
        return true;
    }

    public boolean startBrewing() {
        if (brewing || !result.isEmpty() || ingredientCount() < 2) return false;
        brewing = true;
        brewingTicks = 0;
        accumulatedTemperature = 0f;
        setChanged();
        return true;
    }

    public ItemStack takeResult() {
        if (result.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = result;
        result = ItemStack.EMPTY;
        clearIngredients();
        setChanged();
        return taken;
    }

    public boolean hasResult() {
        return !result.isEmpty();
    }

    public boolean isBrewing() {
        return brewing;
    }

    public int ingredientCount() {
        int count = 0;
        for (ItemStack stack : ingredients) {
            if (!stack.isEmpty()) count++;
        }
        return count;
    }

    public float temperature() {
        return temperature;
    }

    public int brewingTicks() {
        return brewingTicks;
    }

    public List<ItemStack> itemsForDrop() {
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack stack : ingredients) {
            if (!stack.isEmpty()) drops.add(stack.copy());
        }
        if (!result.isEmpty()) drops.add(result.copy());
        return drops;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (!crucible.brewing) return;

        float heatTarget = heatTarget(level.getBlockState(pos.below()));
        float change = heatTarget > crucible.temperature ? 0.5f : -0.15f;
        if (Math.abs(heatTarget - crucible.temperature) < Math.abs(change)) {
            crucible.temperature = heatTarget;
        } else {
            crucible.temperature = Math.max(0f, Math.min(100f, crucible.temperature + change));
        }

        crucible.brewingTicks++;
        crucible.accumulatedTemperature += crucible.temperature;
        if (crucible.brewingTicks >= BREWING_TIME) crucible.finishBrewing();
        crucible.setChanged();
    }

    private void finishBrewing() {
        float averageTemperature = accumulatedTemperature / Math.max(1, brewingTicks);
        List<String> ids = ingredients.stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    ResourceLocation id =
                            net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
                    return id == null ? "" : id.toString();
                })
                .toList();
        PotionQuality quality = CrucibleRecipeLogic.evaluate(ids, averageTemperature);
        result = quality == PotionQuality.CONTAMINATED
                ? new ItemStack(ModItems.CONTAMINATED_MIXTURE.get())
                : SeerPotionItem.create(ModItems.SEER_POTION_9.get(), quality);
        brewing = false;
    }

    private static float heatTarget(BlockState state) {
        if (state.is(Blocks.LAVA)) return 100f;
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) return 90f;
        if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE))
                && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
            return state.is(Blocks.SOUL_CAMPFIRE) ? 70f : 75f;
        }
        return 0f;
    }

    private static boolean isIngredient(ItemStack stack) {
        return stack.is(ModItems.SPIRIT_HERB.get())
                || stack.is(ModItems.DIVINATION_CRYSTAL.get())
                || stack.is(ModItems.MOONWATER.get());
    }

    private int firstEmptySlot() {
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).isEmpty()) return i;
        }
        return -1;
    }

    private void clearIngredients() {
        for (int i = 0; i < ingredients.size(); i++) ingredients.set(i, ItemStack.EMPTY);
        brewingTicks = 0;
        accumulatedTemperature = 0f;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) i);
            ingredients.get(i).save(itemTag);
            list.add(itemTag);
        }
        tag.put("ingredients", list);
        if (!result.isEmpty()) tag.put("result", result.save(new CompoundTag()));
        tag.putInt("brewing_ticks", brewingTicks);
        tag.putFloat("temperature", temperature);
        tag.putFloat("accumulated_temperature", accumulatedTemperature);
        tag.putBoolean("brewing", brewing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        clearIngredients();
        ListTag list = tag.getList("ingredients", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < ingredients.size()) ingredients.set(slot, ItemStack.of(itemTag));
        }
        result = tag.contains("result", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("result")) : ItemStack.EMPTY;
        brewingTicks = tag.getInt("brewing_ticks");
        temperature = tag.getFloat("temperature");
        accumulatedTemperature = tag.getFloat("accumulated_temperature");
        brewing = tag.getBoolean("brewing");
    }
}
