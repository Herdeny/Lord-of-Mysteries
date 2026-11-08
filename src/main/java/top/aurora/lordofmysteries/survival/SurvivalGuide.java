package top.aurora.lordofmysteries.survival;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.aurora.lordofmysteries.registry.ModSurvival;

public final class SurvivalGuide {
    private SurvivalGuide() {}

    public static int show(ServerPlayer player) {
        for (String section : new String[]{"intro", "mining", "farming", "mortar", "exploration"}) {
            player.sendSystemMessage(Component.translatable("guide.lord_of_mysteries.survival." + section)
                    .withStyle(ChatFormatting.AQUA));
        }
        return 1;
    }

    public static int recipes(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.mortar.help"));
        for (MortarRecipe recipe : player.level().getRecipeManager().getAllRecipesFor(ModSurvival.MORTAR_TYPE.get())
                .stream().sorted(java.util.Comparator.comparing(value -> value.getId().toString())).toList()) {
            var ingredients = recipe.getIngredients();
            var main = ingredients.get(0).getItems();
            var reagent = ingredients.get(1).getItems();
            if (main.length == 0 || reagent.length == 0) continue;
            var result = recipe.getResultItem(player.level().registryAccess());
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.mortar.recipe",
                    recipe.mainCount(), main[0].getHoverName(), recipe.reagentCount(), reagent[0].getHoverName(),
                    result.getCount(), result.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        return 1;
    }
}
