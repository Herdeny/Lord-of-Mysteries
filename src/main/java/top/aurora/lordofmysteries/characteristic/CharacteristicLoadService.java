package top.aurora.lordofmysteries.characteristic;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModBlocks;
import top.aurora.lordofmysteries.registry.ModItems;

public final class CharacteristicLoadService {

    public static final int SPIRIT_SALT_COST = 4;
    public static final int SUPPORT_RADIUS = 6;
    public static final long STRAINED_TRAUMA_TICKS = 12000L;
    public static final long FAILURE_TRAUMA_TICKS = 18000L;

    private CharacteristicLoadService() {}

    public static boolean absorb(ServerPlayer player, ItemStack stack) {
        Optional<CharacteristicBundle> incoming =
                CharacteristicConservationService.readStack(stack);
        if (incoming.isEmpty()) {
            sendAbsorptionStatus(player, "invalid");
            return false;
        }
        if (CharacteristicProcessingService.isSealed(stack)) {
            sendAbsorptionStatus(player, "sealed");
            return false;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        CharacteristicLedger.ensurePlayerProvenance(data);
        if (!data.isExtraordinary()) {
            sendAbsorptionStatus(player, "commoner");
            return false;
        }
        int currentIndex = currentBundleIndex(data);
        if (currentIndex < 0) {
            sendAbsorptionStatus(player, "missing_current");
            return false;
        }
        CharacteristicBundle current =
                data.characteristicBundles.get(currentIndex);
        CharacteristicLoadLogic.AbsorptionResult result =
                CharacteristicLoadLogic.absorb(
                        current,
                        incoming.get(),
                        data.sequence);
        if (!result.success()) {
            sendAbsorptionStatus(
                    player, result.status().name().toLowerCase());
            return false;
        }
        if (!audit(
                player,
                "player_absorb",
                List.of(current.sourceHash(), incoming.get().sourceHash()),
                List.of(result.merged().sourceHash()))) {
            return false;
        }

        data.characteristicBundles.set(currentIndex, result.merged());
        float pollutionGain = 6f + incoming.get().corruption() * 0.1f
                + incoming.get().imprint().dominance() * 5f;
        float pressureGain = 10f;
        data.pollution = Math.min(100f, data.pollution + pollutionGain);
        data.insanityPressure = Math.min(
                100f, data.insanityPressure + pressureGain);
        data.roleOveridentification = Math.min(
                100f, data.roleOveridentification + 3f);
        float restored = Math.min(
                CharacteristicLoadLogic.spiritualityReward(result.extraLoad()),
                Math.max(0f, data.spiritualityMax - data.spirituality));
        data.spirituality += restored;
        if (!player.getAbilities().instabuild) stack.shrink(1);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.characteristic.load.absorbed",
                result.extraLoad(),
                oneDecimal(restored),
                oneDecimal(pollutionGain),
                oneDecimal(pressureGain))
                .withStyle(ChatFormatting.DARK_PURPLE));
        return true;
    }

    public static InteractionResult interact(
            ServerLevel level,
            BlockPos separatorPos,
            ServerPlayer player,
            boolean commit) {
        Inspection inspection = inspect(level, separatorPos, player);
        if (!inspection.ready()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.characteristic.load.issue."
                            + inspection.issue().translationSuffix())
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }
        if (!commit) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.characteristic.load.ready",
                    inspection.extraLoad(),
                    Math.round(inspection.stability() * 100f),
                    inspection.supporters())
                    .withStyle(ChatFormatting.AQUA));
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.characteristic.load.commit"));
            return InteractionResult.CONSUME;
        }

        PlayerMysteryData data = MysteryCapability.get(player);
        CharacteristicLedger.ensurePlayerProvenance(data);
        int currentIndex = currentBundleIndex(data);
        if (currentIndex < 0) return InteractionResult.CONSUME;
        CharacteristicBundle current =
                data.characteristicBundles.get(currentIndex);
        CharacteristicLoadLogic.Outcome outcome =
                CharacteristicLoadLogic.resolve(inspection.stability());
        CharacteristicLoadLogic.ExtractionResult result =
                CharacteristicLoadLogic.extract(
                        current,
                        data.sequence,
                        outcome);
        List<String> outputs = result.extracted() == null
                ? List.of(result.retained().sourceHash())
                : List.of(
                        result.retained().sourceHash(),
                        result.extracted().sourceHash());
        if (!audit(
                player,
                "player_extract",
                List.of(current.sourceHash()),
                outputs)) {
            return InteractionResult.CONSUME;
        }
        consumeMaterials(player);
        data.characteristicBundles.set(currentIndex, result.retained());
        applyConsequences(level, separatorPos, player, data, result);
        return InteractionResult.CONSUME;
    }

    public static Inspection inspect(
            ServerLevel level,
            BlockPos separatorPos,
            ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!level.getBlockState(separatorPos)
                .is(ModBlocks.CHARACTERISTIC_SEPARATOR.get())) {
            return Inspection.failure(InspectionIssue.INVALID_WORKSTATION);
        }
        if (!data.isExtraordinary()) {
            return Inspection.failure(InspectionIssue.COMMONER);
        }
        int currentIndex = currentBundleIndex(data);
        if (currentIndex < 0) {
            return Inspection.failure(InspectionIssue.MISSING_CURRENT);
        }
        int extraLoad = CharacteristicLoadLogic.extraLoad(data);
        if (extraLoad <= 0) {
            return Inspection.failure(InspectionIssue.NO_EXTRA_LOAD);
        }
        if (!data.identityAnchored) {
            return Inspection.failure(InspectionIssue.IDENTITY_UNANCHORED);
        }
        if (!player.getMainHandItem().is(
                ModItems.IDENTITY_SALT_CIRCLE.get())
                || !player.getOffhandItem().is(
                ModItems.IMPRINT_WASHING_INCENSE.get())) {
            return Inspection.failure(InspectionIssue.HELD_MATERIALS);
        }
        if (!player.getAbilities().instabuild
                && player.getInventory().countItem(
                ModItems.SPIRIT_SALT.get()) < SPIRIT_SALT_COST) {
            return Inspection.failure(InspectionIssue.SPIRIT_SALT);
        }

        int supporters = supporters(level, separatorPos, player);
        CharacteristicBundle bundle =
                data.characteristicBundles.get(currentIndex);
        float stability = CharacteristicLoadLogic.extractionStability(
                data.pollution,
                data.insanityPressure,
                bundle.imprint().dominance(),
                extraLoad,
                supporters);
        return new Inspection(
                InspectionIssue.READY, extraLoad, supporters, stability);
    }

    public static void sendStatus(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        int extraLoad = CharacteristicLoadLogic.extraLoad(data);
        float multiplier =
                CharacteristicLoadLogic.digestionMultiplier(extraLoad);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.characteristic.load.status",
                extraLoad,
                Math.round(multiplier * 100f))
                .withStyle(extraLoad > 0
                        ? ChatFormatting.DARK_PURPLE : ChatFormatting.GRAY));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.characteristic.load.guide"));
    }

    private static void applyConsequences(
            ServerLevel level,
            BlockPos separatorPos,
            ServerPlayer player,
            PlayerMysteryData data,
            CharacteristicLoadLogic.ExtractionResult result) {
        String suffix;
        ChatFormatting style;
        switch (result.outcome()) {
            case STABLE_SUCCESS -> {
                data.pollution = Math.min(100f, data.pollution + 1f);
                data.insanityPressure = Math.min(
                        100f, data.insanityPressure + 2f);
                suffix = "stable_success";
                style = ChatFormatting.AQUA;
            }
            case STRAINED_SUCCESS -> {
                data.pollution = Math.min(100f, data.pollution + 6f);
                data.insanityPressure = Math.min(
                        100f, data.insanityPressure + 12f);
                data.mentalTraumaEndTick = Math.max(
                        data.mentalTraumaEndTick,
                        level.getGameTime() + STRAINED_TRAUMA_TICKS);
                suffix = "strained_success";
                style = ChatFormatting.GOLD;
            }
            case FAILURE -> {
                data.pollution = Math.min(100f, data.pollution + 12f);
                data.insanityPressure = Math.min(
                        100f, data.insanityPressure + 20f);
                data.mentalTraumaEndTick = Math.max(
                        data.mentalTraumaEndTick,
                        level.getGameTime() + FAILURE_TRAUMA_TICKS);
                suffix = "failure";
                style = ChatFormatting.DARK_RED;
            }
            case NO_EXTRA_LOAD -> {
                suffix = "no_extra_load";
                style = ChatFormatting.YELLOW;
            }
            default -> throw new IllegalStateException(
                    "Unhandled extraction outcome");
        }

        if (result.extracted() != null) {
            ItemStack extracted =
                    CharacteristicConservationService.createStack(
                            result.extracted());
            if (!player.getInventory().add(extracted)) {
                player.drop(extracted, false);
            }
        }
        int remaining = CharacteristicLoadLogic.extraLoad(data);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.characteristic.load."
                        + suffix,
                remaining).withStyle(style));
        level.sendParticles(
                result.outcome().extracted()
                        ? ParticleTypes.ENCHANT : ParticleTypes.SOUL,
                separatorPos.getX() + 0.5d,
                separatorPos.getY() + 1d,
                separatorPos.getZ() + 0.5d,
                result.outcome().extracted() ? 32 : 24,
                0.6d, 0.5d, 0.6d, 0.04d);
        level.playSound(null, separatorPos,
                result.outcome().extracted()
                        ? SoundEvents.ENCHANTMENT_TABLE_USE
                        : SoundEvents.SOUL_ESCAPE,
                result.outcome().extracted()
                        ? SoundSource.PLAYERS : SoundSource.HOSTILE,
                1f,
                result.outcome() == CharacteristicLoadLogic.Outcome
                        .STABLE_SUCCESS ? 1.15f : 0.75f);
    }

    private static int supporters(
            ServerLevel level,
            BlockPos separatorPos,
            ServerPlayer leader) {
        AABB area = new AABB(separatorPos).inflate(
                SUPPORT_RADIUS, 4d, SUPPORT_RADIUS);
        return Math.min(3, level.getEntitiesOfClass(
                ServerPlayer.class,
                area,
                player -> player != leader
                        && player.isShiftKeyDown()
                        && (player.getMainHandItem().is(
                        ModItems.IMPRINT_PROBE.get())
                        || player.getOffhandItem().is(
                        ModItems.IMPRINT_PROBE.get()))).size());
    }

    private static int currentBundleIndex(PlayerMysteryData data) {
        if (data.pathway == null) return -1;
        for (int index = 0;
             index < data.characteristicBundles.size();
             index++) {
            if (data.characteristicBundles.get(index)
                    .pathway().equals(data.pathway)) {
                return index;
            }
        }
        return -1;
    }

    private static void consumeMaterials(ServerPlayer player) {
        if (player.getAbilities().instabuild) return;
        player.getMainHandItem().shrink(1);
        player.getOffhandItem().shrink(1);
        consumeInventoryItem(
                player, ModItems.SPIRIT_SALT.get(), SPIRIT_SALT_COST);
    }

    private static void consumeInventoryItem(
            ServerPlayer player,
            Item item,
            int count) {
        int remaining = count;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(item)) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining == 0) return;
        }
    }

    private static void sendAbsorptionStatus(
            ServerPlayer player,
            String suffix) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.characteristic.load.absorb."
                        + suffix).withStyle(ChatFormatting.YELLOW));
    }

    private static boolean audit(
            ServerPlayer player,
            String operation,
            List<String> inputs,
            List<String> outputs) {
        CharacteristicProvenanceSavedData.ConsumptionResult result =
                CharacteristicProvenanceSavedData.get(player.serverLevel())
                        .consume(
                                operation,
                                player.getUUID(),
                                player.serverLevel().getGameTime(),
                                inputs,
                                outputs);
        if (result == CharacteristicProvenanceSavedData.ConsumptionResult
                .ACCEPTED) {
            return true;
        }
        sendAbsorptionStatus(
                player,
                result == CharacteristicProvenanceSavedData.ConsumptionResult
                        .REPLAY ? "provenance_replay" : "invalid");
        return false;
    }

    private static String oneDecimal(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    public enum InspectionIssue {
        READY("ready"),
        INVALID_WORKSTATION("invalid_workstation"),
        COMMONER("commoner"),
        MISSING_CURRENT("missing_current"),
        NO_EXTRA_LOAD("no_extra_load"),
        IDENTITY_UNANCHORED("identity_unanchored"),
        HELD_MATERIALS("held_materials"),
        SPIRIT_SALT("spirit_salt");

        private final String translationSuffix;

        InspectionIssue(String translationSuffix) {
            this.translationSuffix = translationSuffix;
        }

        public String translationSuffix() {
            return translationSuffix;
        }
    }

    public record Inspection(
            InspectionIssue issue,
            int extraLoad,
            int supporters,
            float stability) {

        public static Inspection failure(InspectionIssue issue) {
            return new Inspection(issue, 0, 0, 0f);
        }

        public boolean ready() {
            return issue == InspectionIssue.READY;
        }
    }
}
