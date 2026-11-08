package top.aurora.lordofmysteries.survival;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import top.aurora.lordofmysteries.registry.ModSurvival;

public final class MortarRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient main;
    private final Ingredient reagent;
    private final int mainCount;
    private final int reagentCount;
    private final ItemStack result;

    public MortarRecipe(ResourceLocation id, Ingredient main, int mainCount,
                        Ingredient reagent, int reagentCount, ItemStack result) {
        if (main.isEmpty() || reagent.isEmpty() || mainCount < 1 || mainCount > 16
                || reagentCount < 1 || reagentCount > 16 || result.isEmpty()
                || result.getCount() > result.getMaxStackSize()) {
            throw new JsonSyntaxException("Invalid mortar ingredients, counts or output");
        }
        this.id = id;
        this.main = main;
        this.reagent = reagent;
        this.mainCount = mainCount;
        this.reagentCount = reagentCount;
        this.result = result.copy();
    }

    public int mainCount() { return mainCount; }
    public int reagentCount() { return reagentCount; }

    @Override
    public boolean matches(Container input, Level level) {
        return input.getContainerSize() == 2 && main.test(input.getItem(0))
                && reagent.test(input.getItem(1)) && input.getItem(0).getCount() >= mainCount
                && input.getItem(1).getCount() >= reagentCount;
    }

    @Override
    public ItemStack assemble(Container input, RegistryAccess access) { return result.copy(); }
    @Override
    public ItemStack getResultItem(RegistryAccess access) { return result.copy(); }
    @Override
    public ResourceLocation getId() { return id; }
    @Override
    public RecipeSerializer<?> getSerializer() { return ModSurvival.MORTAR_SERIALIZER.get(); }
    @Override
    public RecipeType<?> getType() { return ModSurvival.MORTAR_TYPE.get(); }
    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override
    public boolean isSpecial() { return true; }
    @Override
    public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, main, reagent); }

    public static final class Serializer implements RecipeSerializer<MortarRecipe> {
        @Override
        public MortarRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new MortarRecipe(id, Ingredient.fromJson(json.get("main")),
                    GsonHelper.getAsInt(json, "main_count", 1),
                    Ingredient.fromJson(json.get("reagent")),
                    GsonHelper.getAsInt(json, "reagent_count", 1),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")));
        }

        @Override
        public MortarRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new MortarRecipe(id, Ingredient.fromNetwork(buffer), buffer.readVarInt(),
                    Ingredient.fromNetwork(buffer), buffer.readVarInt(), buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MortarRecipe recipe) {
            recipe.main.toNetwork(buffer);
            buffer.writeVarInt(recipe.mainCount);
            recipe.reagent.toNetwork(buffer);
            buffer.writeVarInt(recipe.reagentCount);
            buffer.writeItem(recipe.result);
        }
    }
}
