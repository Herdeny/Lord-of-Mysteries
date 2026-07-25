package top.aurora.lordofmysteries.ritual;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModEntities;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.MistCityWorldEvent;
import top.aurora.lordofmysteries.world.MistCityWorldEventModifiers;
import top.aurora.lordofmysteries.world.MistCityWorldEventPolicy;

public final class SequenceFiveAdvancementRitual {

    public enum Type {
        MARIONETTE("seer"),
        DREAMWALKER("spectator"),
        REAPER("hunter"),
        DREAM_THIEF("thief"),
        TRAVELER("apprentice");

        private final ResourceLocation pathway;

        Type(String pathway) {
            this.pathway = id(pathway);
        }

        public ResourceLocation pathway() {
            return pathway;
        }

        public ResourceLocation knowledge() {
            return id("knowledge/sequence_five_ritual/" + pathway.getPath());
        }

        public String translationSuffix() {
            return pathway.getPath();
        }
    }

    private enum Issue {
        READY,
        ALREADY_COMPLETE,
        WRONG_PATHWAY,
        WRONG_SEQUENCE,
        DIGESTION_INCOMPLETE,
        CIRCLE_INCOMPLETE,
        REQUIREMENTS_MISSING
    }

    private record Cost(Item item, int count) {}

    private record Inspection(Type type, Issue issue, int supporters,
                              float stability) {
        boolean ready() {
            return issue == Issue.READY;
        }
    }

    private static final ResourceLocation ADVANCEMENT =
            id("complete_sequence_five_ritual");
    private static final int SUPPORT_RADIUS = 7;

    private SequenceFiveAdvancementRitual() {}

    public static boolean isSequenceFivePotion(ItemStack stack) {
        return typeForPotion(stack) != null;
    }

    public static boolean hasCompleted(PlayerMysteryData data,
                                       ResourceLocation pathway) {
        Type type = TypeForPathway.find(pathway);
        return type != null && data.knownKnowledge.contains(type.knowledge());
    }

    public static InteractionResult interact(ServerLevel level, BlockPos altarPos,
                                             ServerPlayer player, ItemStack potion,
                                             boolean commit) {
        Type type = typeForPotion(potion);
        if (type == null) return InteractionResult.PASS;

        Inspection inspection = inspect(level, altarPos, player, type);
        if (!inspection.ready()) {
            sendIssue(player, inspection);
            return InteractionResult.CONSUME;
        }
        if (!commit) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.sequence_five_ritual.ready",
                    Component.translatable(typeNameKey(type)),
                    inspection.supporters(),
                    Math.round(inspection.stability() * 100f))
                    .withStyle(ChatFormatting.AQUA));
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.sequence_five_ritual.commit"));
            return InteractionResult.CONSUME;
        }

        consumeCosts(player, costs(type));
        SequenceFiveRitualLogic.Outcome outcome =
                SequenceFiveRitualLogic.resolve(
                        inspection.stability(), level.random.nextFloat());
        applyOutcome(level, altarPos, player, type, inspection.supporters(),
                outcome);
        return InteractionResult.CONSUME;
    }

    public static void appendPotionTooltip(int targetSequence,
                                           List<Component> tooltip) {
        if (targetSequence != 5) return;
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.potion.sequence_five_ritual")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static int showGuide(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        Type type = TypeForPathway.find(data.pathway);
        if (type == null) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.sequence_five_ritual.guide.general")
                    .withStyle(ChatFormatting.GRAY));
            return 1;
        }
        boolean complete = data.knownKnowledge.contains(type.knowledge());
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.sequence_five_ritual.guide.status",
                Component.translatable(typeNameKey(type)),
                data.sequence,
                Math.round(data.digestion),
                Component.translatable(complete
                        ? "message.lord_of_mysteries.sequence_five_ritual.guide.complete"
                        : "message.lord_of_mysteries.sequence_five_ritual.guide.pending"))
                .withStyle(complete
                        ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.sequence_five_ritual.requirements."
                        + type.translationSuffix(),
                Component.translatable(typeNameKey(type))));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.sequence_five_ritual.guide.controls"));
        return 1;
    }

    private static Inspection inspect(ServerLevel level, BlockPos altarPos,
                                      ServerPlayer player, Type type) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.knownKnowledge.contains(type.knowledge())) {
            return new Inspection(type, Issue.ALREADY_COMPLETE, 0, 1f);
        }
        if (!type.pathway().equals(data.pathway)) {
            return new Inspection(type, Issue.WRONG_PATHWAY, 0, 0f);
        }
        if (data.sequence != 6) {
            return new Inspection(type, Issue.WRONG_SEQUENCE, 0, 0f);
        }
        if (data.digestion < 100f) {
            return new Inspection(type, Issue.DIGESTION_INCOMPLETE, 0, 0f);
        }
        if (!MultiBlockRitualDetector.inspect(level, altarPos).complete()) {
            return new Inspection(type, Issue.CIRCLE_INCOMPLETE, 0, 0f);
        }
        if (!player.getAbilities().instabuild
                && (!hasCosts(player, costs(type))
                || !environmentValid(level, altarPos, type, player))) {
            return new Inspection(type, Issue.REQUIREMENTS_MISSING, 0, 0f);
        }
        if (player.getAbilities().instabuild
                && !environmentValid(level, altarPos, type, player)) {
            return new Inspection(type, Issue.REQUIREMENTS_MISSING, 0, 0f);
        }

        int supporters = supporters(level, altarPos, player, type);
        float stability = SequenceFiveRitualLogic.stability(
                data.pollution,
                data.insanityPressure,
                supporters,
                worldEventBonus(level));
        return new Inspection(type, Issue.READY, supporters, stability);
    }

    private static void sendIssue(ServerPlayer player, Inspection inspection) {
        String key = switch (inspection.issue()) {
            case ALREADY_COMPLETE ->
                    "message.lord_of_mysteries.sequence_five_ritual.already_complete";
            case WRONG_PATHWAY ->
                    "message.lord_of_mysteries.sequence_five_ritual.wrong_pathway";
            case WRONG_SEQUENCE ->
                    "message.lord_of_mysteries.sequence_five_ritual.wrong_sequence";
            case DIGESTION_INCOMPLETE ->
                    "message.lord_of_mysteries.sequence_five_ritual.digestion";
            case CIRCLE_INCOMPLETE ->
                    "message.lord_of_mysteries.sequence_five_ritual.circle";
            case REQUIREMENTS_MISSING ->
                    "message.lord_of_mysteries.sequence_five_ritual.requirements."
                            + inspection.type().translationSuffix();
            case READY ->
                    "message.lord_of_mysteries.sequence_five_ritual.ready";
        };
        PlayerFeedback.send(player, Component.translatable(
                key, Component.translatable(typeNameKey(inspection.type())))
                .withStyle(inspection.issue() == Issue.ALREADY_COMPLETE
                        ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
    }

    private static void applyOutcome(ServerLevel level, BlockPos altarPos,
                                     ServerPlayer player, Type type,
                                     int supporters,
                                     SequenceFiveRitualLogic.Outcome outcome) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (outcome.success()) {
            data.knownKnowledge.add(type.knowledge());
            if (outcome == SequenceFiveRitualLogic.Outcome.STRAINED_SUCCESS) {
                data.insanityPressure = Math.min(
                        100f, data.insanityPressure + 5f);
                data.pollution = Math.min(100f, data.pollution + 2f);
            }
            grantAdvancement(player);
            level.sendParticles(
                    outcome == SequenceFiveRitualLogic.Outcome.STABLE_SUCCESS
                            ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT,
                    altarPos.getX() + 0.5,
                    altarPos.getY() + 1,
                    altarPos.getZ() + 0.5,
                    outcome == SequenceFiveRitualLogic.Outcome.STABLE_SUCCESS
                            ? 42 : 28,
                    0.9, 0.6, 0.9, 0.04);
            level.playSound(null, altarPos,
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                    1f, outcome == SequenceFiveRitualLogic.Outcome.STABLE_SUCCESS
                            ? 1.15f : 0.85f);
            PlayerFeedback.send(player, Component.translatable(
                    outcome == SequenceFiveRitualLogic.Outcome.STABLE_SUCCESS
                            ? "message.lord_of_mysteries.sequence_five_ritual.success"
                            : "message.lord_of_mysteries.sequence_five_ritual.strained_success",
                    Component.translatable(typeNameKey(type)), supporters)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            notifySupporters(level, altarPos, player, type);
            return;
        }

        if (outcome == SequenceFiveRitualLogic.Outcome.FAILURE) {
            data.insanityPressure = Math.min(
                    100f, data.insanityPressure + 12f);
            data.pollution = Math.min(100f, data.pollution + 6f);
        } else {
            data.insanityPressure = Math.min(
                    100f, data.insanityPressure + 20f);
            data.pollution = Math.min(100f, data.pollution + 15f);
            spawnWraith(level, altarPos, player);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                altarPos.getX() + 0.5,
                altarPos.getY() + 1,
                altarPos.getZ() + 0.5,
                32, 0.9, 0.5, 0.9, 0.05);
        level.playSound(null, altarPos, SoundEvents.SOUL_ESCAPE,
                SoundSource.HOSTILE, 1f, 0.7f);
        PlayerFeedback.send(player, Component.translatable(
                outcome == SequenceFiveRitualLogic.Outcome.FAILURE
                        ? "message.lord_of_mysteries.sequence_five_ritual.failure"
                        : "message.lord_of_mysteries.sequence_five_ritual.severe_failure",
                Component.translatable(typeNameKey(type)))
                .withStyle(ChatFormatting.DARK_RED));
    }

    private static boolean environmentValid(ServerLevel level, BlockPos altarPos,
                                            Type type, ServerPlayer leader) {
        return switch (type) {
            case MARIONETTE ->
                    !level.isDay() && level.canSeeSky(altarPos.above());
            case DREAMWALKER -> hasBlock(level, altarPos, 4,
                    state -> state.is(BlockTags.BEDS));
            case REAPER -> hasBlock(level, altarPos, 4,
                    state -> state.getBlock() instanceof CampfireBlock
                            && state.getValue(CampfireBlock.LIT));
            case DREAM_THIEF ->
                    hasSleepingWitness(level, altarPos, leader)
                            || (!level.isDay() && hasBlock(
                            level, altarPos, 4,
                            state -> state.is(BlockTags.BEDS)));
            case TRAVELER -> distinctLodestoneCompasses(leader) >= 2;
        };
    }

    private static boolean hasSleepingWitness(ServerLevel level,
                                              BlockPos altarPos,
                                              ServerPlayer leader) {
        AABB area = new AABB(altarPos).inflate(6d, 3d, 6d);
        return !level.getEntitiesOfClass(
                LivingEntity.class, area,
                entity -> entity != leader && entity.isSleeping()).isEmpty();
    }

    private static boolean hasBlock(ServerLevel level, BlockPos center,
                                    int radius,
                                    java.util.function.Predicate<BlockState> test) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    cursor.setWithOffset(center, x, y, z);
                    if (test.test(level.getBlockState(cursor))) return true;
                }
            }
        }
        return false;
    }

    private static int supporters(ServerLevel level, BlockPos altarPos,
                                  ServerPlayer leader, Type type) {
        Item focus = supportFocus(type);
        AABB area = new AABB(altarPos).inflate(
                SUPPORT_RADIUS, 4d, SUPPORT_RADIUS);
        return Math.min(3, level.getEntitiesOfClass(
                ServerPlayer.class, area,
                player -> player != leader
                        && player.isShiftKeyDown()
                        && (player.getMainHandItem().is(focus)
                        || player.getOffhandItem().is(focus))).size());
    }

    private static void notifySupporters(ServerLevel level, BlockPos altarPos,
                                         ServerPlayer leader, Type type) {
        Item focus = supportFocus(type);
        AABB area = new AABB(altarPos).inflate(
                SUPPORT_RADIUS, 4d, SUPPORT_RADIUS);
        for (ServerPlayer player : level.getEntitiesOfClass(
                ServerPlayer.class, area,
                player -> player != leader
                        && player.isShiftKeyDown()
                        && (player.getMainHandItem().is(focus)
                        || player.getOffhandItem().is(focus)))) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.sequence_five_ritual.supported",
                    leader.getDisplayName(),
                    Component.translatable(typeNameKey(type)))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    private static List<Cost> costs(Type type) {
        return switch (type) {
            case MARIONETTE -> List.of(
                    new Cost(ModItems.WHITE_CANDLE.get(), 5),
                    new Cost(Items.STRING, 1));
            case DREAMWALKER -> List.of(
                    new Cost(ModItems.WHITE_CANDLE.get(), 2),
                    new Cost(ModItems.DREAM_SCALE_FRAGMENT.get(), 1));
            case REAPER -> List.of(
                    new Cost(Items.BONE, 5),
                    new Cost(ModItems.EMBER_SALAMANDER_GLAND.get(), 1));
            case DREAM_THIEF -> List.of(
                    new Cost(ModItems.DREAM_SCALE_FRAGMENT.get(), 1),
                    new Cost(Items.AMETHYST_SHARD, 1));
            case TRAVELER -> List.of(
                    new Cost(ModItems.STARLIGHT_MOSS.get(), 2),
                    new Cost(ModItems.MIRROR_CRAB_SHELL.get(), 1));
        };
    }

    private static Item supportFocus(Type type) {
        return switch (type) {
            case MARIONETTE -> Items.STRING;
            case DREAMWALKER -> Items.FEATHER;
            case REAPER -> Items.SHIELD;
            case DREAM_THIEF -> Items.AMETHYST_SHARD;
            case TRAVELER -> Items.COMPASS;
        };
    }

    private static boolean hasCosts(ServerPlayer player, List<Cost> costs) {
        for (Cost cost : costs) {
            int count = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(cost.item())) count += stack.getCount();
            }
            if (count < cost.count()) return false;
        }
        return true;
    }

    private static void consumeCosts(ServerPlayer player, List<Cost> costs) {
        if (player.getAbilities().instabuild) return;
        for (Cost cost : costs) {
            int remaining = cost.count();
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.is(cost.item())) continue;
                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                remaining -= consumed;
                if (remaining == 0) break;
            }
        }
    }

    private static int distinctLodestoneCompasses(ServerPlayer player) {
        Set<String> anchors = new HashSet<>();
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(Items.COMPASS) || !stack.hasTag()) continue;
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains("LodestonePos", Tag.TAG_COMPOUND)
                    || !tag.contains("LodestoneDimension", Tag.TAG_STRING)) {
                continue;
            }
            CompoundTag position = tag.getCompound("LodestonePos");
            anchors.add(tag.getString("LodestoneDimension")
                    + ":" + position.getInt("X")
                    + ":" + position.getInt("Y")
                    + ":" + position.getInt("Z"));
        }
        return anchors.size();
    }

    private static float worldEventBonus(ServerLevel level) {
        long eventDay = Math.max(0L, Math.floorDiv(
                level.getServer().overworld().getDayTime(), 24_000L));
        MistCityWorldEvent event = MistCityWorldEventPolicy.eventForDay(
                level.getServer().overworld().getSeed(), eventDay);
        return MistCityWorldEventModifiers.ritualCompletionBonus(event);
    }

    private static Type typeForPotion(ItemStack stack) {
        if (stack.is(ModItems.SEER_POTION_5.get())) return Type.MARIONETTE;
        if (stack.is(ModItems.SPECTATOR_POTION_5.get())) return Type.DREAMWALKER;
        if (stack.is(ModItems.HUNTER_POTION_5.get())) return Type.REAPER;
        if (stack.is(ModItems.THIEF_POTION_5.get())) return Type.DREAM_THIEF;
        if (stack.is(ModItems.APPRENTICE_POTION_5.get())) return Type.TRAVELER;
        return null;
    }

    private static String typeNameKey(Type type) {
        return "ritual.lord_of_mysteries.sequence_five."
                + type.translationSuffix();
    }

    private static void grantAdvancement(ServerPlayer player) {
        if (player.connection == null) return;
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(ADVANCEMENT);
        if (advancement == null) return;
        AdvancementProgress progress = player.getAdvancements()
                .getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    private static void spawnWraith(ServerLevel level, BlockPos altarPos,
                                    ServerPlayer target) {
        var entity = ModEntities.SEER_BREAKDOWN.get().create(level);
        if (entity == null) return;
        entity.moveTo(altarPos.getX() + 0.5,
                altarPos.getY() + 1,
                altarPos.getZ() + 0.5,
                level.random.nextFloat() * 360f, 0f);
        entity.setTarget(target);
        level.addFreshEntity(entity);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
    }

    private static final class TypeForPathway {
        private TypeForPathway() {}

        private static Type find(ResourceLocation pathway) {
            if (pathway == null) return null;
            for (Type type : Type.values()) {
                if (type.pathway().equals(pathway)) return type;
            }
            return null;
        }
    }
}
