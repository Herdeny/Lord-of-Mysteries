package top.aurora.lordofmysteries.survival;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public final class SurvivalLootModifier extends LootModifier {
    public static final Codec<SurvivalLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).and(instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(value -> value.item),
                    Codec.intRange(1, 8).fieldOf("count").forGetter(value -> value.count)))
                    .apply(instance, SurvivalLootModifier::new));
    private final Item item;
    private final int count;

    public SurvivalLootModifier(LootItemCondition[] conditions, Item item, int count) {
        super(conditions);
        this.item = item;
        this.count = count;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        loot.add(new ItemStack(item, count));
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
