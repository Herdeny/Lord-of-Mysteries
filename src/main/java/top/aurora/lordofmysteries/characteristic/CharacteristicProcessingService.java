package top.aurora.lordofmysteries.characteristic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class CharacteristicProcessingService {

    private static final String PROCESSING_PAYLOAD = "CharacteristicProcessing";
    private static final String SEALED = "Sealed";
    private static final String OPERATION_COUNT = "OperationCount";
    private static final String LAST_OPERATION = "LastOperation";

    private CharacteristicProcessingService() {}

    public static StackResult split(ItemStack source) {
        Optional<CharacteristicBundle> bundle =
                CharacteristicConservationService.readStack(source);
        if (bundle.isEmpty()) return StackResult.failure(Status.INVALID);
        if (isSealed(source)) return StackResult.failure(Status.SEALED);
        CharacteristicProcessingLogic.SplitResult result =
                CharacteristicProcessingLogic.splitHighestLayer(bundle.get());
        if (!result.success()) {
            return StackResult.failure(map(result.status()));
        }
        ItemStack extracted = processedStack(
                source, result.extracted(), "split-extracted", false);
        ItemStack remainder = processedStack(
                source, result.remainder(), "split-remainder", false);
        return StackResult.success(extracted, remainder);
    }

    public static StackResult split(
            ServerPlayer player, ItemStack source) {
        return audit(player, "item_split", split(source), source);
    }

    public static StackResult merge(ItemStack first, ItemStack second) {
        Optional<CharacteristicBundle> firstBundle =
                CharacteristicConservationService.readStack(first);
        Optional<CharacteristicBundle> secondBundle =
                CharacteristicConservationService.readStack(second);
        if (firstBundle.isEmpty() || secondBundle.isEmpty()) {
            return StackResult.failure(Status.INVALID);
        }
        if (isSealed(first) || isSealed(second)) {
            return StackResult.failure(Status.SEALED);
        }
        CharacteristicProcessingLogic.MergeResult result =
                CharacteristicProcessingLogic.merge(
                        firstBundle.get(), secondBundle.get());
        if (!result.success()) {
            return StackResult.failure(map(result.status()));
        }
        return StackResult.success(processedStack(
                first, result.merged(), "merge", false), ItemStack.EMPTY);
    }

    public static StackResult merge(
            ServerPlayer player, ItemStack first, ItemStack second) {
        return audit(
                player, "item_merge", merge(first, second), first, second);
    }

    public static StackResult cleanse(ItemStack source) {
        Optional<CharacteristicBundle> bundle =
                CharacteristicConservationService.readStack(source);
        if (bundle.isEmpty()) return StackResult.failure(Status.INVALID);
        if (isSealed(source)) return StackResult.failure(Status.SEALED);
        CharacteristicProcessingLogic.CleanseResult result =
                CharacteristicProcessingLogic.cleanse(bundle.get());
        if (!result.success()) {
            return StackResult.failure(map(result.status()));
        }
        return StackResult.success(processedStack(
                source, result.cleansed(), "cleanse", false), ItemStack.EMPTY);
    }

    public static StackResult cleanse(
            ServerPlayer player, ItemStack source) {
        return audit(player, "item_cleanse", cleanse(source), source);
    }

    public static StackResult seal(ItemStack source) {
        Optional<CharacteristicBundle> bundle =
                CharacteristicConservationService.readStack(source);
        if (bundle.isEmpty()) return StackResult.failure(Status.INVALID);
        if (isSealed(source)) {
            return StackResult.failure(Status.ALREADY_SEALED);
        }
        return StackResult.success(processedStack(
                source, bundle.get(), "seal", true), ItemStack.EMPTY);
    }

    public static StackResult unseal(ItemStack source) {
        Optional<CharacteristicBundle> bundle =
                CharacteristicConservationService.readStack(source);
        if (bundle.isEmpty()) return StackResult.failure(Status.INVALID);
        if (!isSealed(source)) {
            return StackResult.failure(Status.NOT_SEALED);
        }
        return StackResult.success(processedStack(
                source, bundle.get(), "unseal", false), ItemStack.EMPTY);
    }

    public static boolean isSealed(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(PROCESSING_PAYLOAD)
                && tag.getCompound(PROCESSING_PAYLOAD).getBoolean(SEALED);
    }

    public static int operationCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(PROCESSING_PAYLOAD)) return 0;
        return Math.max(0, tag.getCompound(PROCESSING_PAYLOAD)
                .getInt(OPERATION_COUNT));
    }

    public static void sendInspection(ServerPlayer player, ItemStack stack) {
        Optional<CharacteristicBundle> bundle =
                CharacteristicConservationService.readStack(stack);
        if (bundle.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.characteristic.invalid")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        CharacteristicBundle value = bundle.get();
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.inspect",
                value.highestSequence(),
                CharacteristicProcessingLogic.totalUnits(value),
                Math.round(CharacteristicProcessingLogic.averagePurity(value)
                        * 100f),
                Math.round(value.corruption()),
                Math.round(value.imprint().dominance() * 100f),
                value.imprint().cleansingCount(),
                value.sourceHash().substring(0, 8),
                isSealed(stack)
                        ? Component.translatable(
                                "message.lord_of_mysteries.characteristic.sealed")
                        : Component.translatable(
                                "message.lord_of_mysteries.characteristic.unsealed"))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static int sendGuide(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.guide.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.guide.separator"));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.guide.washing"));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.guide.safety")
                .withStyle(ChatFormatting.GRAY));
        CharacteristicLoadService.sendStatus(player);
        return 1;
    }

    public static int sendAudit(ServerPlayer player) {
        CharacteristicProvenanceSavedData ledger =
                CharacteristicProvenanceSavedData.get(player.serverLevel());
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.characteristic.audit",
                ledger.consumedSourceCount(),
                ledger.operationCount("item_split"),
                ledger.operationCount("item_merge"),
                ledger.operationCount("item_cleanse"),
                ledger.operationCount("player_absorb"),
                ledger.operationCount("player_extract"))
                .withStyle(ChatFormatting.DARK_AQUA));
        return Math.max(1, ledger.consumedSourceCount());
    }

    public static Component statusMessage(Status status) {
        return Component.translatable(
                "message.lord_of_mysteries.characteristic.status."
                        + status.translationSuffix());
    }

    private static StackResult audit(
            ServerPlayer player,
            String operation,
            StackResult result,
            ItemStack... inputs) {
        if (!result.success()) return result;
        List<String> inputSources = new ArrayList<>(inputs.length);
        for (ItemStack input : inputs) {
            Optional<CharacteristicBundle> bundle =
                    CharacteristicConservationService.readStack(input);
            if (bundle.isEmpty()) return StackResult.failure(Status.INVALID);
            inputSources.add(bundle.get().sourceHash());
        }
        List<String> outputSources = new ArrayList<>(2);
        addOutputSource(outputSources, result.primary());
        addOutputSource(outputSources, result.secondary());
        CharacteristicProvenanceSavedData.ConsumptionResult audit =
                CharacteristicProvenanceSavedData.get(player.serverLevel())
                        .consume(
                                operation,
                                player.getUUID(),
                                player.serverLevel().getGameTime(),
                                inputSources,
                                outputSources);
        return switch (audit) {
            case ACCEPTED -> result;
            case REPLAY -> StackResult.failure(Status.PROVENANCE_REPLAY);
            case INVALID -> StackResult.failure(Status.INVALID);
        };
    }

    private static void addOutputSource(
            List<String> outputSources, ItemStack stack) {
        CharacteristicConservationService.readStack(stack)
                .map(CharacteristicBundle::sourceHash)
                .ifPresent(outputSources::add);
    }

    private static ItemStack processedStack(
            ItemStack template,
            CharacteristicBundle bundle,
            String operation,
            boolean sealed) {
        ItemStack result = template.copy();
        result.setCount(1);
        CharacteristicConservationService.writeStack(result, bundle);
        CompoundTag processing = result.getOrCreateTag()
                .getCompound(PROCESSING_PAYLOAD);
        processing.putBoolean(SEALED, sealed);
        processing.putInt(OPERATION_COUNT,
                operationCount(template) + 1);
        processing.putString(LAST_OPERATION, operation);
        result.getOrCreateTag().put(PROCESSING_PAYLOAD, processing);
        return result;
    }

    private static Status map(CharacteristicProcessingLogic.Status status) {
        return switch (status) {
            case SUCCESS -> Status.SUCCESS;
            case SINGLE_UNIT -> Status.SINGLE_UNIT;
            case PATHWAY_MISMATCH -> Status.PATHWAY_MISMATCH;
            case DUPLICATE_SOURCE -> Status.DUPLICATE_SOURCE;
            case LAYER_CAPACITY -> Status.LAYER_CAPACITY;
            case ALREADY_CLEAN -> Status.ALREADY_CLEAN;
        };
    }

    public enum Status {
        SUCCESS("success"),
        INVALID("invalid"),
        SEALED("sealed_locked"),
        ALREADY_SEALED("already_sealed"),
        NOT_SEALED("not_sealed"),
        SINGLE_UNIT("single_unit"),
        PATHWAY_MISMATCH("pathway_mismatch"),
        DUPLICATE_SOURCE("duplicate_source"),
        PROVENANCE_REPLAY("provenance_replay"),
        LAYER_CAPACITY("layer_capacity"),
        ALREADY_CLEAN("already_clean");

        private final String translationSuffix;

        Status(String translationSuffix) {
            this.translationSuffix = translationSuffix;
        }

        public String translationSuffix() {
            return translationSuffix;
        }
    }

    public record StackResult(
            Status status,
            ItemStack primary,
            ItemStack secondary) {

        public static StackResult success(
                ItemStack primary, ItemStack secondary) {
            return new StackResult(Status.SUCCESS, primary, secondary);
        }

        public static StackResult failure(Status status) {
            return new StackResult(
                    status, ItemStack.EMPTY, ItemStack.EMPTY);
        }

        public boolean success() {
            return status == Status.SUCCESS;
        }
    }
}
