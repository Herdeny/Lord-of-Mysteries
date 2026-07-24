package top.aurora.lordofmysteries.commission;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import top.aurora.lordofmysteries.knowledge.M1TrialTracker;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.MistCityDistrictLayout;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;
import top.aurora.lordofmysteries.world.MistCityWorldEvent;
import top.aurora.lordofmysteries.world.MistCityWorldEventPolicy;

public final class CityLifeService {

    public static final int PAPER_COST = 3;
    public static final long SHIFT_REWARD_PENCE = 24L;
    private static final double WORK_DISTANCE_SQUARED = 64d;

    private CityLifeService() {}

    public static boolean tryWorkPressShift(ServerPlayer player) {
        if (!player.isShiftKeyDown()) return false;
        performShift(player, CityEconomyPolicy.Job.PRESS);
        return true;
    }

    public static int workFromServiceNpc(
            ServerPlayer player, CityEconomyPolicy.Job job) {
        return performShift(player, job);
    }

    public static int workAtDistrict(
            ServerPlayer player, CityEconomyPolicy.Job job) {
        if (!isNearDistrict(player, job)) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.city_life.not_near",
                            Component.translatable(job.translationKey()))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        return performShift(player, job);
    }

    private static int performShift(
            ServerPlayer player, CityEconomyPolicy.Job job) {
        PlayerMysteryData data = MysteryCapability.get(player);
        ServerLevel cityLevel = player.getServer().overworld();
        long day = Math.max(
                0L, Math.floorDiv(cityLevel.getDayTime(), 24_000L));
        if (!CityEconomyPolicy.canWork(data.lastCityWorkDay, day)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.city_life.already_worked")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        MistCityWorldEvent worldEvent =
                MistCityWorldEventPolicy.eventForDay(
                        cityLevel.getSeed(), day);
        CityEconomyPolicy.ShiftTerms terms =
                CityEconomyPolicy.terms(job, worldEvent);
        if (player.getInventory().countItem(Items.PAPER)
                < terms.paperCost()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.city_life.need_paper",
                    terms.paperCost()).withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        consumePaper(player, terms.paperCost());
        data.lastCityWorkDay = day;
        data.cityWorkShifts++;
        recordJob(data, job);
        data.moneyPence = saturatingAdd(
                data.moneyPence, terms.rewardPence());
        data.insanityPressure = Math.min(
                100f, data.insanityPressure + terms.pressureIncrease());
        data.mysticalExposure = MysticalExposurePolicy.adjust(
                data.mysticalExposure, -terms.exposureReduction());
        giveJobSupplies(player, job);
        data.markDirty(PlayerDataSection.SOCIAL);
        data.markDirty(PlayerDataSection.CORE);
        M1TrialTracker.recordStreetLife(player);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.city_life.shift_complete_v2",
                Component.translatable(job.translationKey()),
                CommissionCurrency.format(terms.rewardPence()),
                Math.round(terms.pressureIncrease()),
                Math.round(terms.exposureReduction()),
                CommissionCurrency.format(data.moneyPence),
                Component.translatable(worldEvent.translationKey()))
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    public static int showStatus(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        ServerLevel cityLevel = player.getServer().overworld();
        long day = Math.max(
                0L, Math.floorDiv(cityLevel.getDayTime(), 24_000L));
        MistCityWorldEvent worldEvent =
                MistCityWorldEventPolicy.eventForDay(
                        cityLevel.getSeed(), day);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.life.status_v2",
                CommissionCurrency.format(data.moneyPence),
                data.cityWorkShifts,
                data.lastCityWorkDay == day
                        ? Component.translatable(
                                "command.lord_of_mysteries.life.done")
                        : Component.translatable(
                                "command.lord_of_mysteries.life.available"),
                Math.round(data.mysticalExposure),
                Component.translatable(
                        MysticalExposurePolicy.band(
                                data.mysticalExposure).translationKey()),
                Component.translatable(worldEvent.translationKey()))
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.life.jobs",
                data.pressWorkShifts,
                data.agencyWorkShifts,
                data.patrolWorkShifts)
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.life.hint_v2", PAPER_COST)
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static boolean isNearDistrict(
            ServerPlayer player, CityEconomyPolicy.Job job) {
        ServerLevel level = player.getServer().getLevel(Level.OVERWORLD);
        if (level == null || player.level() != level) return false;
        BlockPos outpost = MistCityOutpostSavedData.get(level)
                .outpost().orElse(null);
        if (outpost == null) return false;
        MistCityDistrictLayout.District district = switch (job) {
            case PRESS -> MistCityDistrictLayout.District.PRESS;
            case AGENCY -> MistCityDistrictLayout.District.DETECTIVE_AGENCY;
            case PATROL -> MistCityDistrictLayout.District.CONSTABULARY;
        };
        BlockPos service = MistCityOutpostSavedData.get(level)
                .serviceVersion() >= 2
                ? MistCityDistrictLayout.servicePosition(outpost, district)
                : switch (job) {
                    case PRESS -> outpost.offset(-2, 1, -1);
                    case AGENCY -> outpost.offset(-5, 1, 3);
                    case PATROL -> outpost.offset(5, 1, 3);
                };
        return player.distanceToSqr(
                service.getX() + 0.5d,
                service.getY(),
                service.getZ() + 0.5d) <= WORK_DISTANCE_SQUARED;
    }

    private static void recordJob(
            PlayerMysteryData data, CityEconomyPolicy.Job job) {
        switch (job) {
            case PRESS -> data.pressWorkShifts++;
            case AGENCY -> data.agencyWorkShifts++;
            case PATROL -> data.patrolWorkShifts++;
        }
    }

    private static void giveJobSupplies(
            ServerPlayer player, CityEconomyPolicy.Job job) {
        switch (job) {
            case PRESS -> {
                giveItem(player, new ItemStack(Items.BREAD, 2));
                giveItem(player, new ItemStack(ModItems.NEWSPAPER.get()));
            }
            case AGENCY -> {
                giveItem(player, new ItemStack(Items.PAPER, 2));
                giveItem(player, new ItemStack(Items.TORCH, 2));
            }
            case PATROL -> {
                giveItem(player, new ItemStack(Items.BREAD, 3));
                giveItem(player, new ItemStack(Items.TORCH, 4));
            }
        }
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        player.containerMenu.broadcastChanges();
    }

    private static long saturatingAdd(long value, long increase) {
        if (increase > 0L && value > Long.MAX_VALUE - increase) {
            return Long.MAX_VALUE;
        }
        return value + increase;
    }

    private static void consumePaper(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(Items.PAPER)) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining == 0) break;
        }
        player.containerMenu.broadcastChanges();
    }
}
