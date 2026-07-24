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
import net.minecraft.world.item.Items;
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
        CrucibleRecipeLogic.BrewResult brew =
                CrucibleRecipeLogic.evaluateRecipe(ids, averageTemperature);
        result = switch (brew.potion()) {
            case SEER_9 -> SeerPotionItem.create(ModItems.SEER_POTION_9.get(), brew.quality());
            case SEER_8 -> SeerPotionItem.create(ModItems.SEER_POTION_8.get(), brew.quality());
            case SEER_7 -> SeerPotionItem.create(ModItems.SEER_POTION_7.get(), brew.quality());
            case SEER_6 -> SeerPotionItem.create(ModItems.SEER_POTION_6.get(), brew.quality());
            case SEER_5 -> SeerPotionItem.create(ModItems.SEER_POTION_5.get(), brew.quality());
            case SPECTATOR_9 ->
                    SeerPotionItem.create(ModItems.SPECTATOR_POTION_9.get(), brew.quality());
            case SPECTATOR_8 ->
                    SeerPotionItem.create(ModItems.SPECTATOR_POTION_8.get(), brew.quality());
            case SPECTATOR_7 ->
                    SeerPotionItem.create(ModItems.SPECTATOR_POTION_7.get(), brew.quality());
            case SPECTATOR_6 ->
                    SeerPotionItem.create(ModItems.SPECTATOR_POTION_6.get(), brew.quality());
            case SPECTATOR_5 ->
                    SeerPotionItem.create(ModItems.SPECTATOR_POTION_5.get(), brew.quality());
            case HUNTER_9 ->
                    SeerPotionItem.create(ModItems.HUNTER_POTION_9.get(), brew.quality());
            case HUNTER_8 ->
                    SeerPotionItem.create(ModItems.HUNTER_POTION_8.get(), brew.quality());
            case HUNTER_7 ->
                    SeerPotionItem.create(ModItems.HUNTER_POTION_7.get(), brew.quality());
            case HUNTER_6 ->
                    SeerPotionItem.create(ModItems.HUNTER_POTION_6.get(), brew.quality());
            case HUNTER_5 ->
                    SeerPotionItem.create(ModItems.HUNTER_POTION_5.get(), brew.quality());
            case THIEF_9 ->
                    SeerPotionItem.create(ModItems.THIEF_POTION_9.get(), brew.quality());
            case THIEF_8 ->
                    SeerPotionItem.create(ModItems.THIEF_POTION_8.get(), brew.quality());
            case THIEF_7 ->
                    SeerPotionItem.create(ModItems.THIEF_POTION_7.get(), brew.quality());
            case THIEF_6 ->
                    SeerPotionItem.create(ModItems.THIEF_POTION_6.get(), brew.quality());
            case THIEF_5 ->
                    SeerPotionItem.create(ModItems.THIEF_POTION_5.get(), brew.quality());
            case APPRENTICE_9 ->
                    SeerPotionItem.create(ModItems.APPRENTICE_POTION_9.get(), brew.quality());
            case APPRENTICE_8 ->
                    SeerPotionItem.create(ModItems.APPRENTICE_POTION_8.get(), brew.quality());
            case APPRENTICE_7 ->
                    SeerPotionItem.create(ModItems.APPRENTICE_POTION_7.get(), brew.quality());
            case APPRENTICE_6 ->
                    SeerPotionItem.create(ModItems.APPRENTICE_POTION_6.get(), brew.quality());
            case APPRENTICE_5 ->
                    SeerPotionItem.create(ModItems.APPRENTICE_POTION_5.get(), brew.quality());
            case CONTAMINATED -> new ItemStack(ModItems.CONTAMINATED_MIXTURE.get());
        };
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
                || stack.is(ModItems.MOONWATER.get())
                || stack.is(ModItems.DEEP_GRAY_SPIRIT_TEAR.get())
                || stack.is(ModItems.HEATHER.get())
                || stack.is(ModItems.SPIRIT_ALCOHOL.get())
                || stack.is(ModItems.SHAPESHIFTER_SERPENT_GLAND.get())
                || stack.is(ModItems.ASH_POWDER.get())
                || stack.is(ModItems.SILVER_FILINGS.get())
                || stack.is(ModItems.SPIRIT_SALT.get())
                || stack.is(ModItems.ASHEN_THREAD.get())
                || stack.is(ModItems.WHITE_CANDLE.get())
                || stack.is(ModItems.SHADOW_MARTEN_CLAW.get())
                || stack.is(ModItems.STARLIGHT_MOSS.get())
                || stack.is(ModItems.MYSTIC_INK.get())
                || stack.is(ModItems.DREAM_SCALE_FRAGMENT.get())
                || stack.is(ModItems.EMBER_SALAMANDER_GLAND.get())
                || stack.is(ModItems.MIRROR_CRAB_SHELL.get())
                || stack.is(ModItems.ANCIENT_TABLET_SPORE_SAC.get())
                || stack.is(ModItems.IRIDESCENT_TRICKBIRD_FEATHER.get())
                || stack.is(ModItems.METEOR_DUST.get())
                || stack.is(ModItems.BLANK_MANUSCRIPT.get())
                || stack.is(Items.FERMENTED_SPIDER_EYE)
                || stack.is(Items.HONEY_BOTTLE)
                || stack.is(Items.BOOK)
                || stack.is(Items.AMETHYST_SHARD)
                || stack.is(Items.BONE)
                || stack.is(Items.RABBIT_FOOT)
                || stack.is(Items.GUNPOWDER)
                || stack.is(Items.REDSTONE)
                || stack.is(Items.BLAZE_POWDER)
                || stack.is(Items.SLIME_BALL)
                || stack.is(Items.GOLD_NUGGET)
                || stack.is(Items.MOSS_BLOCK)
                || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.COMPASS);
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
