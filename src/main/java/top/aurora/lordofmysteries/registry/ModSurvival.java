package top.aurora.lordofmysteries.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.survival.MortarRecipe;
import top.aurora.lordofmysteries.survival.SurvivalLootModifier;

public final class ModSurvival {
    private ModSurvival() {}
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ProjectMystery.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ProjectMystery.MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ProjectMystery.MOD_ID);
    public static final RegistryObject<RecipeType<MortarRecipe>> MORTAR_TYPE = TYPES.register("mystic_mortar",
            () -> new RecipeType<>() { public String toString() { return "lord_of_mysteries:mystic_mortar"; } });
    public static final RegistryObject<RecipeSerializer<MortarRecipe>> MORTAR_SERIALIZER =
            SERIALIZERS.register("mystic_mortar", MortarRecipe.Serializer::new);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SUPPLIES =
            LOOT.register("survival_supplies", () -> SurvivalLootModifier.CODEC);

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
        LOOT.register(bus);
    }
}
