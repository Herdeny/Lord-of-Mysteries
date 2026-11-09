package top.aurora.lordofmysteries.survival;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SurvivalEconomy {
    private SurvivalEconomy() {}

    public static void registerComposting() {
        ComposterBlock.COMPOSTABLES.put(ModItems.SPIRIT_HERB.get(), 0.65f);
        ComposterBlock.COMPOSTABLES.put(ModItems.HEATHER.get(), 0.65f);
    }

    @SubscribeEvent
    public static void villagers(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.FARMER) {
            event.getTrades().get(2).add(sell(ModItems.SPIRIT_HERB.get(), 4, 1, 8, 5));
            event.getTrades().get(2).add(sell(ModItems.HEATHER.get(), 4, 1, 8, 5));
            event.getTrades().get(3).add(buy(ModItems.SPIRIT_HERB.get(), 24));
            event.getTrades().get(3).add(buy(ModItems.HEATHER.get(), 24));
        } else if (event.getType() == VillagerProfession.CLERIC) {
            event.getTrades().get(2).add(sell(ModItems.SPIRIT_SALT.get(), 3, 1, 8, 5));
            event.getTrades().get(3).add(sell(ModItems.MYSTIC_INK.get(), 1, 3, 4, 10));
        }
    }

    @SubscribeEvent
    public static void wanderers(WandererTradesEvent event) {
        event.getGenericTrades().add(sell(ModItems.SPIRIT_HERB.get(), 2, 1, 4, 0));
        event.getGenericTrades().add(sell(ModItems.HEATHER.get(), 2, 1, 4, 0));
    }

    private static VillagerTrades.ItemListing sell(Item item, int count, int emeralds, int uses, int experience) {
        return (trader, random) -> new MerchantOffer(new ItemStack(Items.EMERALD, emeralds),
                new ItemStack(item, count), uses, experience, 0.05f);
    }

    private static VillagerTrades.ItemListing buy(Item item, int count) {
        return (trader, random) -> new MerchantOffer(new ItemStack(item, count),
                new ItemStack(Items.EMERALD), 8, 10, 0.05f);
    }
}
