package top.aurora.lordofmysteries.artifact;

import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.commission.CommissionCurrency;
import top.aurora.lordofmysteries.commission.MysticalExposurePolicy;
import top.aurora.lordofmysteries.organization.OrganizationDefinitionManager;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SealedArtifactService {

    public static final String INSTANCE_TAG = "ArtifactCustodyInstance";
    public static final String ARTIFACT_TAG = "ArtifactDefinition";
    public static final String QUARANTINED_TAG = "ArtifactQuarantined";
    public static final String MASK_ORGANIZATION_TAG =
            "ArtifactMaskOrganization";
    public static final int TRUSTED_REPUTATION = 8;
    public static final int STABILIZE_INCENSE_COST = 1;
    public static final int STABILIZE_WATER_COST = 1;
    private static final double SERVICE_DISTANCE_SQUARED = 100d;

    private SealedArtifactService() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % 100 != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level != null) {
            ArtifactCustodySavedData.get(level).expireOverdue(
                    Math.max(0L, Math.floorDiv(
                            level.getDayTime(), 24_000L)));
        }
    }

    public static InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand,
            ManagedArtifactKind kind) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !allowUse(serverPlayer, stack, kind)) {
            return InteractionResultHolder.fail(stack);
        }
        boolean applied = switch (kind) {
            case KINDLY_UMBRELLA ->
                    useUmbrella(serverPlayer, stack);
            case HONEST_MIRROR -> useMirror(serverPlayer, stack);
            case SLEEPING_BELL -> useSleepingBell(serverPlayer, stack);
            case GUEST_MASK -> useGuestMask(serverPlayer, stack);
            case CITY_WHISTLE -> useCityWhistle(serverPlayer, stack);
            case MERCIFUL_CHAIN, ETERNAL_MATCHBOX -> false;
        };
        if (!applied) return InteractionResultHolder.fail(stack);
        recordUse(serverPlayer, stack, kind);
        return InteractionResultHolder.success(stack);
    }

    public static InteractionResult interact(
            ItemStack stack, Player player,
            LivingEntity target, InteractionHand hand,
            ManagedArtifactKind kind) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || kind != ManagedArtifactKind.MERCIFUL_CHAIN
                || target instanceof Player
                || !allowUse(serverPlayer, stack, kind)) {
            return InteractionResult.FAIL;
        }
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 240, 4));
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 240, 3));
        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 240, 0));
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        target.getPersistentData().putUUID(
                "MercifulChainCustodian", serverPlayer.getUUID());
        target.getPersistentData().putLong(
                "MercifulChainUntil",
                serverPlayer.serverLevel().getGameTime() + 240L);
        applyCost(serverPlayer, stack.getItem(), 8f, 1f);
        serverPlayer.serverLevel().playSound(
                null, target.blockPosition(),
                SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS,
                0.8f, 0.75f);
        recordUse(serverPlayer, stack, kind);
        return InteractionResult.SUCCESS;
    }

    public static void observeInventory(
            ItemStack stack, Level level, Entity entity,
            ManagedArtifactKind kind) {
        if (level.isClientSide()
                || !(entity instanceof ServerPlayer player)
                || player.tickCount % 100 != 0) {
            return;
        }
        UUID instance = instanceId(stack);
        if (instance == null) {
            if (kind == ManagedArtifactKind.ETERNAL_MATCHBOX
                    && adoptLegacy(player, stack, kind)) {
                return;
            }
            stack.getOrCreateTag().putBoolean(
                    QUARANTINED_TAG, true);
            return;
        }
        if (countInstances(player, instance) > 1) {
            ledger(player).markAbused(instance, 10);
            stack.getOrCreateTag().putBoolean(
                    QUARANTINED_TAG, true);
            return;
        }
        ArtifactCustodySavedData ledger = ledger(player);
        ArtifactCustodySavedData.Observation observation =
                ledger.observe(
                        instance, kind.id(), player.getUUID(),
                        player.level().dimension().location(),
                        player.blockPosition(),
                        currentDay(player),
                        player.serverLevel().getGameTime());
        ArtifactCustodySavedData.CustodyRecord record =
                ledger.record(instance);
        if (observation == ArtifactCustodySavedData.Observation
                .OVERDUE_LEAK
                || record != null
                && record.state() == ArtifactCustodyState.LEAKED
                && !stack.getOrCreateTag()
                        .getBoolean("sealed")) {
            stack.getOrCreateTag().putBoolean("sealed", true);
            applyLeak(player, definition(kind));
        } else if (observation == ArtifactCustodySavedData.Observation
                .DUPLICATE
                || observation == ArtifactCustodySavedData.Observation
                .INVALID
                || observation == ArtifactCustodySavedData.Observation
                .RETURNED) {
            stack.getOrCreateTag().putBoolean(
                    QUARANTINED_TAG, true);
        }
    }

    public static boolean allowUse(
            ServerPlayer player, ItemStack stack,
            ManagedArtifactKind kind) {
        if (stack.hasTag()
                && (stack.getTag().getBoolean(QUARANTINED_TAG)
                || stack.getTag().getBoolean("sealed"))) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.use_denied")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        UUID instance = instanceId(stack);
        if (instance == null
                && !(kind == ManagedArtifactKind.ETERNAL_MATCHBOX
                && adoptLegacy(player, stack, kind))) {
            stack.getOrCreateTag().putBoolean(
                    QUARANTINED_TAG, true);
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.unregistered")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        instance = instanceId(stack);
        ArtifactCustodySavedData ledger = ledger(player);
        ArtifactCustodySavedData.Observation observation =
                ledger.observe(
                        instance, kind.id(), player.getUUID(),
                        player.level().dimension().location(),
                        player.blockPosition(),
                        currentDay(player),
                        player.serverLevel().getGameTime());
        ArtifactCustodySavedData.CustodyRecord record =
                ledger.record(instance);
        if (observation == ArtifactCustodySavedData.Observation
                .DUPLICATE
                || observation == ArtifactCustodySavedData.Observation
                .INVALID
                || observation == ArtifactCustodySavedData.Observation
                .RETURNED
                || record == null
                || record.state() != ArtifactCustodyState.BORROWED) {
            stack.getOrCreateTag().putBoolean(
                    QUARANTINED_TAG, true);
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.use_denied")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        return true;
    }

    public static void recordUse(
            ServerPlayer player, ItemStack stack,
            ManagedArtifactKind kind) {
        UUID instance = instanceId(stack);
        SealedArtifactDefinition definition = definition(kind);
        if (instance == null || definition == null) return;
        ArtifactCustodyState state = ledger(player).recordUse(
                instance,
                definition.dangerLevel(),
                definition.safeUses(),
                definition.leakThreshold());
        if (state == ArtifactCustodyState.LEAKED) {
            stack.getOrCreateTag().putBoolean("sealed", true);
            applyLeak(player, definition);
        }
    }

    public static int showGuide(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.guide")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.guide.commands")
                .withStyle(ChatFormatting.GRAY));
        return showStatus(player);
    }

    public static int showCatalog(ServerPlayer player) {
        var definitions = SealedArtifactDefinitionManager.all()
                .values().stream()
                .sorted(Comparator.comparing(
                        value -> value.id().toString()))
                .toList();
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.catalog",
                        definitions.size())
                .withStyle(ChatFormatting.GOLD));
        for (SealedArtifactDefinition definition : definitions) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.catalog.entry",
                            definition.id().getPath(),
                            Component.translatable(definition.titleKey()),
                            definition.dangerLevel(),
                            definition.loanDays())
                    .withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.catalog.detail",
                            Component.translatable(
                                    definition.effectKey()),
                            Component.translatable(
                                    definition.costKey()),
                            definition.safeUses(),
                            definition.leakThreshold())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        return definitions.size();
    }

    public static int showStatus(ServerPlayer player) {
        ArtifactCustodySavedData ledger = ledger(player);
        var visibleRecords = ledger.records().stream()
                .filter(record ->
                        record.responsible().equals(player.getUUID())
                        || record.holder().equals(player.getUUID()))
                .toList();
        long active = visibleRecords.stream()
                .filter(record -> record.state().active())
                .count();
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.status",
                        active, visibleRecords.size(),
                        player.hasPermissions(2)
                                ? ledger.orphanedCount() : 0)
                .withStyle(ChatFormatting.GOLD));
        for (ArtifactCustodySavedData.CustodyRecord record
                : visibleRecords) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.status.entry",
                            record.artifactId().getPath(),
                            record.state().id(),
                            record.contamination(),
                            record.dueDay(),
                            record.blockPosition().toShortString())
                    .withStyle(record.state()
                            == ArtifactCustodyState.LEAKED
                            ? ChatFormatting.RED
                            : ChatFormatting.GRAY));
        }
        return 1;
    }

    public static int borrow(
            ServerPlayer player, String rawArtifactId) {
        if (!nearOutpost(player)) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.not_at_vault")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ResourceLocation artifactId = normalizeArtifactId(rawArtifactId);
        SealedArtifactDefinition definition =
                artifactId == null ? null
                        : SealedArtifactDefinitionManager.get(artifactId);
        Item item = artifactId == null ? null : itemFor(artifactId);
        if (definition == null || item == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.unknown",
                            rawArtifactId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        int reputation = data.orgReputation.getOrDefault(
                definition.custodyOrganization(), 0);
        if (reputation < TRUSTED_REPUTATION) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.reputation",
                            TRUSTED_REPUTATION, reputation)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ArtifactCustodySavedData ledger = ledger(player);
        if (ledger.activeForDefinition(artifactId) != null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.unavailable")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        UUID instance = ledger.issue(
                definition, player.getUUID(), currentDay(player),
                player.serverLevel().getGameTime(),
                player.level().dimension().location(),
                player.blockPosition());
        if (instance == null) return 0;
        ItemStack stack = new ItemStack(item);
        bind(stack, artifactId, instance);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
            ledger.markDropped(
                    instance,
                    player.level().dimension().location(),
                    player.blockPosition(),
                    player.serverLevel().getGameTime());
        }
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.borrowed",
                        Component.translatable(definition.titleKey()),
                        definition.loanDays())
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    public static int stabilize(ServerPlayer player) {
        BoundStack bound = firstBoundStack(player);
        if (bound == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.none_held")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ArtifactCustodySavedData ledger = ledger(player);
        ArtifactCustodySavedData.CustodyRecord record =
                ledger.record(bound.instance());
        if (record == null
                || record.state() != ArtifactCustodyState.LEAKED) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.not_leaking")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (player.getInventory().countItem(
                ModItems.CALMING_INCENSE.get())
                < STABILIZE_INCENSE_COST
                || player.getInventory().countItem(
                ModItems.PURE_WATER.get()) < STABILIZE_WATER_COST) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.stabilize_cost")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!ledger.stabilize(
                bound.instance(), player.getUUID())) return 0;
        consume(player, ModItems.CALMING_INCENSE.get(),
                STABILIZE_INCENSE_COST);
        consume(player, ModItems.PURE_WATER.get(),
                STABILIZE_WATER_COST);
        bound.stack().getOrCreateTag().putBoolean("sealed", true);
        bound.stack().getOrCreateTag().remove(QUARANTINED_TAG);
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.stabilized")
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    public static int returnHeld(ServerPlayer player) {
        return returnFirstHeld(player, false);
    }

    public static int returnFirstHeld(
            ServerPlayer player, boolean fromOrganizationAction) {
        if (!nearOutpost(player)) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.not_at_vault")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        BoundStack bound = firstBoundStack(player);
        if (bound == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.none_held")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ArtifactCustodySavedData ledger = ledger(player);
        ArtifactCustodySavedData.CustodyRecord record =
                ledger.record(bound.instance());
        if (record == null || record.state()
                == ArtifactCustodyState.ABUSED
                || !ledger.returnToVault(
                        bound.instance(), player.getUUID())) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.return_denied")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        bound.stack().shrink(1);
        PlayerMysteryData data = MysteryCapability.get(player);
        int reputation = record.state()
                == ArtifactCustodyState.RECOVERED ? 3 : 1;
        long reward = record.state()
                == ArtifactCustodyState.RECOVERED ? 36L : 12L;
        if (!fromOrganizationAction) {
            data.orgReputation.merge(
                    record.organizationId(), reputation, Integer::sum);
            data.moneyPence = saturatingAdd(data.moneyPence, reward);
            data.markDirty(PlayerDataSection.SOCIAL);
        }
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.translatable(
                        fromOrganizationAction
                                ? "command.lord_of_mysteries.artifact.transferred"
                                : "command.lord_of_mysteries.artifact.returned",
                        reputation,
                        CommissionCurrency.format(reward))
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    public static int setGuestMaskOrganization(
            ServerPlayer player, String rawOrganization) {
        ResourceLocation organization =
                normalizeOrganizationId(rawOrganization);
        if (organization == null
                || OrganizationDefinitionManager.get(organization)
                == null) {
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.organization.unknown",
                            rawOrganization)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        for (int slot = 0;
             slot < player.getInventory().getContainerSize();
             slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.ARTIFACT_GUEST_MASK.get())) continue;
            stack.getOrCreateTag().putString(
                    MASK_ORGANIZATION_TAG, organization.toString());
            player.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.mask_set",
                            organization.getPath())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return 1;
        }
        player.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.mask_missing")
                .withStyle(ChatFormatting.RED));
        return 0;
    }

    public static int showLedger(ServerPlayer player) {
        ArtifactCustodySavedData ledger = ledger(player);
        player.sendSystemMessage(Component.literal(
                        "Artifact custody ledger: "
                                + ledger.records().size()
                                + " records, "
                                + ledger.orphanedCount()
                                + " orphaned")
                .withStyle(ChatFormatting.GOLD));
        ledger.records().forEach(record ->
                player.sendSystemMessage(Component.literal(
                                record.instanceId() + " "
                                        + record.artifactId() + " "
                                        + record.state().id() + " holder="
                                        + record.holder() + " pos="
                                        + record.dimension() + " "
                                        + record.blockPosition()
                                                .toShortString())
                        .withStyle(ChatFormatting.GRAY)));
        return ledger.records().size();
    }

    public static int retireAbused(
            ServerPlayer operator, String rawInstance) {
        UUID instance;
        try {
            instance = UUID.fromString(rawInstance);
        } catch (IllegalArgumentException exception) {
            operator.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.retire_invalid")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!ledger(operator).retireAbused(instance)) {
            operator.sendSystemMessage(Component.translatable(
                            "command.lord_of_mysteries.artifact.retire_denied")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        operator.sendSystemMessage(Component.translatable(
                        "command.lord_of_mysteries.artifact.retired",
                        instance)
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getEntity().getItem();
        UUID instance = instanceId(stack);
        if (instance == null) return;
        ledger(player).markDropped(
                instance,
                player.level().dimension().location(),
                event.getEntity().blockPosition(),
                player.serverLevel().getGameTime());
    }

    private static boolean useUmbrella(
            ServerPlayer player, ItemStack stack) {
        if (!player.serverLevel().isRaining()
                || !player.serverLevel().canSeeSky(
                        player.blockPosition())) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.umbrella_dry")
                    .withStyle(ChatFormatting.GRAY));
            return false;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 600, 0));
        player.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING, 600, 0));
        int promises = stack.getOrCreateTag().getInt(
                "ArtifactPromiseDebt") + 1;
        stack.getOrCreateTag().putInt(
                "ArtifactPromiseDebt", promises);
        if (promises % 3 == 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 300, 0));
        }
        applyCost(player, stack.getItem(), 4f, 0f);
        return true;
    }

    private static boolean useMirror(
            ServerPlayer player, ItemStack stack) {
        AABB area = player.getBoundingBox().inflate(16d);
        int revealed = 0;
        for (LivingEntity target : player.serverLevel()
                .getEntitiesOfClass(LivingEntity.class, area,
                        entity -> entity != player
                                && !(entity instanceof Player))) {
            if (revealed >= 16) break;
            target.removeEffect(MobEffects.INVISIBILITY);
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, 240, 0));
            revealed++;
        }
        applyCost(player, stack.getItem(), 4f, 2f);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.mirror_result",
                        revealed)
                .withStyle(ChatFormatting.AQUA));
        return true;
    }

    private static boolean useSleepingBell(
            ServerPlayer player, ItemStack stack) {
        AABB area = player.getBoundingBox().inflate(12d);
        int affected = 0;
        for (LivingEntity target : player.serverLevel()
                .getEntitiesOfClass(LivingEntity.class, area,
                        entity -> entity != player
                                && !(entity instanceof Player))) {
            if (affected >= 24) break;
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 240, 2));
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 240, 1));
            affected++;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        player.addEffect(new MobEffectInstance(
                MobEffects.DARKNESS, 120, 0));
        applyCost(player, stack.getItem(), 10f, 2f);
        player.serverLevel().playSound(
                null, player.blockPosition(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS,
                1f, 0.55f);
        return affected > 0;
    }

    private static boolean useGuestMask(
            ServerPlayer player, ItemStack stack) {
        ResourceLocation organization = ResourceLocation.tryParse(
                stack.getOrCreateTag().getString(
                        MASK_ORGANIZATION_TAG));
        if (organization == null) {
            organization = bestKnownOrganization(player);
            if (organization != null) {
                stack.getOrCreateTag().putString(
                        MASK_ORGANIZATION_TAG,
                        organization.toString());
            }
        }
        if (organization == null
                || MysteryCapability.get(player)
                .orgReputation.getOrDefault(organization, 0) < 4) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.mask_denied")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 600, 0));
        player.getPersistentData().putString(
                "ArtifactMaskedOrganization",
                organization.toString());
        player.getPersistentData().putLong(
                "ArtifactMaskedUntil",
                player.serverLevel().getGameTime() + 600L);
        applyCost(player, stack.getItem(), 6f, 1f);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.mask_active",
                        organization.getPath())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return true;
    }

    private static boolean useCityWhistle(
            ServerPlayer player, ItemStack stack) {
        if (!nearOutpost(player)) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.whistle_area")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        AABB area = player.getBoundingBox().inflate(32d);
        int alerted = 0;
        for (Mob mob : player.serverLevel().getEntitiesOfClass(
                Mob.class, area,
                entity -> entity.getType().getCategory()
                        == MobCategory.MONSTER)) {
            if (alerted >= 32) break;
            mob.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, 300, 0));
            alerted++;
        }
        player.serverLevel().playSound(
                null, player.blockPosition(),
                SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(),
                SoundSource.PLAYERS, 2f, 1.2f);
        applyCost(player, stack.getItem(), 12f, 5f);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.whistle_result",
                        alerted)
                .withStyle(ChatFormatting.RED));
        return true;
    }

    private static void applyCost(
            ServerPlayer player, Item sourceItem,
            float pressure, float exposure) {
        PlayerMysteryData data = MysteryCapability.get(player);
        data.insanityPressure = Math.min(
                100f, data.insanityPressure + pressure);
        data.mysticalExposure = MysticalExposurePolicy.adjust(
                data.mysticalExposure, exposure);
        data.markDirty(PlayerDataSection.CORE);
        if (exposure != 0f) data.markDirty(PlayerDataSection.SOCIAL);
        if (sourceItem != null) {
            player.getCooldowns().addCooldown(sourceItem, 100);
        }
    }

    private static void applyLeak(
            ServerPlayer player,
            SealedArtifactDefinition definition) {
        int danger = definition == null ? 3
                : definition.dangerLevel();
        applyCost(player, null, danger * 3f, danger);
        player.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION, 200, 0));
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.leaked",
                        danger)
                .withStyle(ChatFormatting.DARK_RED));
    }

    private static boolean adoptLegacy(
            ServerPlayer player, ItemStack stack,
            ManagedArtifactKind kind) {
        SealedArtifactDefinition definition = definition(kind);
        if (definition == null) return false;
        ArtifactCustodySavedData ledger = ledger(player);
        if (ledger.activeForDefinition(kind.id()) != null) {
            return false;
        }
        UUID instance = ledger.issue(
                definition, player.getUUID(), currentDay(player),
                player.serverLevel().getGameTime(),
                player.level().dimension().location(),
                player.blockPosition());
        if (instance == null) return false;
        bind(stack, kind.id(), instance);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.legacy_adopted")
                .withStyle(ChatFormatting.GOLD));
        return true;
    }

    private static SealedArtifactDefinition definition(
            ManagedArtifactKind kind) {
        return SealedArtifactDefinitionManager.get(kind.id());
    }

    private static ArtifactCustodySavedData ledger(
            ServerPlayer player) {
        return ArtifactCustodySavedData.get(
                player.getServer().overworld());
    }

    private static long currentDay(ServerPlayer player) {
        return Math.max(0L, Math.floorDiv(
                player.getServer().overworld().getDayTime(), 24_000L));
    }

    private static boolean nearOutpost(ServerPlayer player) {
        ServerLevel level = player.getServer().overworld();
        if (player.level() != level) return false;
        BlockPos outpost = MistCityOutpostSavedData.get(level)
                .outpost().orElse(null);
        return outpost != null && outpost.distSqr(
                player.blockPosition()) <= SERVICE_DISTANCE_SQUARED;
    }

    private static UUID instanceId(ItemStack stack) {
        return stack.hasTag()
                && stack.getTag().hasUUID(INSTANCE_TAG)
                ? stack.getTag().getUUID(INSTANCE_TAG)
                : null;
    }

    private static void bind(
            ItemStack stack, ResourceLocation artifact, UUID instance) {
        stack.getOrCreateTag().putUUID(INSTANCE_TAG, instance);
        stack.getOrCreateTag().putString(
                ARTIFACT_TAG, artifact.toString());
        stack.getOrCreateTag().remove(QUARANTINED_TAG);
        stack.getOrCreateTag().remove("sealed");
    }

    private static BoundStack firstBoundStack(ServerPlayer player) {
        ArtifactCustodySavedData ledger = ledger(player);
        for (int slot = 0;
             slot < player.getInventory().getContainerSize();
             slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            UUID instance = instanceId(stack);
            if (instance == null) continue;
            ArtifactCustodySavedData.CustodyRecord record =
                    ledger.record(instance);
            ResourceLocation tagArtifact = stack.hasTag()
                    ? ResourceLocation.tryParse(
                            stack.getTag().getString(ARTIFACT_TAG))
                    : null;
            if (record == null || tagArtifact == null
                    || !tagArtifact.equals(record.artifactId())
                    || stack.getItem() != itemFor(
                            record.artifactId())) {
                stack.getOrCreateTag().putBoolean(
                        QUARANTINED_TAG, true);
                continue;
            }
            return new BoundStack(stack, instance);
        }
        return null;
    }

    private static int countInstances(
            ServerPlayer player, UUID instance) {
        int matches = 0;
        for (int slot = 0;
             slot < player.getInventory().getContainerSize();
             slot++) {
            if (instance.equals(instanceId(
                    player.getInventory().getItem(slot)))) {
                matches++;
            }
        }
        return matches;
    }

    private static ResourceLocation normalizeArtifactId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String value = raw.trim().toLowerCase(Locale.ROOT);
        return ResourceLocation.tryParse(
                value.contains(":")
                        ? value
                        : ProjectMystery.MOD_ID + ":" + value);
    }

    private static ResourceLocation normalizeOrganizationId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (!value.contains(":")) {
            value = ProjectMystery.MOD_ID + ":organization/" + value;
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id != null && !id.getPath().startsWith("organization/")) {
            return null;
        }
        return id;
    }

    private static ResourceLocation bestKnownOrganization(
            ServerPlayer player) {
        return MysteryCapability.get(player).orgReputation.entrySet()
                .stream()
                .filter(entry ->
                        OrganizationDefinitionManager.get(
                                entry.getKey()) != null)
                .filter(entry -> entry.getValue() >= 4)
                .max(Comparator
                        .<java.util.Map.Entry<ResourceLocation, Integer>>
                                comparingInt(java.util.Map.Entry::getValue)
                        .thenComparing(entry ->
                                entry.getKey().toString()))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
    }

    private static Item itemFor(ResourceLocation artifactId) {
        for (ManagedArtifactKind kind : ManagedArtifactKind.values()) {
            if (!kind.id().equals(artifactId)) continue;
            return switch (kind) {
                case ETERNAL_MATCHBOX -> ModItems.ETERNAL_MATCHBOX.get();
                case KINDLY_UMBRELLA ->
                        ModItems.ARTIFACT_KINDLY_UMBRELLA.get();
                case HONEST_MIRROR ->
                        ModItems.ARTIFACT_HONEST_MIRROR.get();
                case SLEEPING_BELL ->
                        ModItems.ARTIFACT_SLEEPING_BELL.get();
                case GUEST_MASK -> ModItems.ARTIFACT_GUEST_MASK.get();
                case MERCIFUL_CHAIN ->
                        ModItems.ARTIFACT_MERCIFUL_CHAIN.get();
                case CITY_WHISTLE ->
                        ModItems.ARTIFACT_CITY_WHISTLE.get();
            };
        }
        return null;
    }

    private static void consume(
            ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int slot = 0;
             slot < player.getInventory().getContainerSize()
                     && remaining > 0;
             slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
        }
        player.containerMenu.broadcastChanges();
    }

    private static long saturatingAdd(long value, long increase) {
        if (increase > 0L && value > Long.MAX_VALUE - increase) {
            return Long.MAX_VALUE;
        }
        return value + increase;
    }

    private record BoundStack(ItemStack stack, UUID instance) {}
}
