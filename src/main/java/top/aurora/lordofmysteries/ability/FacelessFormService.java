package top.aurora.lordofmysteries.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FacelessFormService {

    public static final float RECORD_COST = 10f;
    public static final float TRANSFORM_COST = 25f;
    public static final float TRANSFORM_DRAIN_PER_SECOND = 2f;
    public static final long RECORD_DURATION_TICKS = 60L;
    public static final long RECORD_COOLDOWN_TICKS = 200L;
    public static final long TRANSFORM_COOLDOWN_TICKS = 1200L;
    public static final double RECORD_RANGE = 12d;

    private static final String ACTIVE_FORM =
            ProjectMystery.MOD_ID + ":faceless_form";
    private static final String ACTIVE_NAME =
            ProjectMystery.MOD_ID + ":faceless_name";
    private static final Map<UUID, PendingRecord> PENDING = new HashMap<>();
    private static final Map<UUID, ActiveDisguise> ACTIVE = new HashMap<>();

    private FacelessFormService() {}

    public static boolean startRecording(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasFacelessAbility(data)) return unavailable(player);
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(
                data.facelessRecordCooldownEndTick, now)) {
            return cooldown(
                    player, data.facelessRecordCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(
                player, RECORD_RANGE);
        if (target == null || target == player) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.ability.no_target"));
            return false;
        }
        if (!FacelessFormPolicy.canRecord(target)) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.player_target_forbidden"));
            return false;
        }
        if (PENDING.containsKey(player.getUUID())) {
            cancelRecording(player, "restarted");
        }
        PENDING.put(player.getUUID(), new PendingRecord(
                target.getUUID(), now, now + RECORD_DURATION_TICKS));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.record_started",
                target.getDisplayName(), RECORD_DURATION_TICKS / 20L)
                .withStyle(ChatFormatting.GRAY));
        return true;
    }

    public static boolean toggleDisguise(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasFacelessAbility(data)) return unavailable(player);
        if (ACTIVE.containsKey(player.getUUID())) {
            deactivate(player, false);
            return true;
        }
        List<CompoundTag> records =
                FacelessFormPolicy.normalizeRecords(data.facelessFormRecords);
        int selected = FacelessFormPolicy.normalizeSelection(
                records, data.facelessSelectedForm);
        if (selected < 0) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.empty"));
            return false;
        }
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(
                data.facelessDisguiseCooldownEndTick, now)) {
            return cooldown(
                    player, data.facelessDisguiseCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, TRANSFORM_COST)) {
            return insufficient(player, TRANSFORM_COST);
        }

        CompoundTag record = records.get(selected);
        String displayName = FacelessFormPolicy.displayName(record);
        data.facelessFormRecords = records;
        data.facelessSelectedForm = selected;
        data.facelessDisguiseCooldownEndTick =
                AbilityCooldowns.start(now, TRANSFORM_COOLDOWN_TICKS);
        ACTIVE.put(player.getUUID(), new ActiveDisguise(
                FacelessFormPolicy.recordId(record),
                player.getCustomName(),
                player.isCustomNameVisible(),
                now));
        player.getPersistentData().putUUID(
                ACTIVE_FORM, FacelessFormPolicy.recordId(record));
        player.getPersistentData().putString(ACTIVE_NAME, displayName);
        player.setCustomName(Component.literal(displayName)
                .withStyle(ChatFormatting.GRAY));
        player.setCustomNameVisible(true);
        applyDisguiseEffects(player);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.activated",
                displayName, TRANSFORM_DRAIN_PER_SECOND)
                .withStyle(ChatFormatting.GRAY));
        player.level().playSound(
                null, player.blockPosition(),
                SoundEvents.ILLUSIONER_CAST_SPELL,
                SoundSource.PLAYERS, 0.65f, 0.9f);
        ActingEventHandler.trigger(
                player, ActingEvent.FACELESS6_MAINTAIN_COVER, null);
        return true;
    }

    public static int cycle(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasFacelessAbility(data)) {
            unavailable(player);
            return 0;
        }
        List<CompoundTag> records =
                FacelessFormPolicy.normalizeRecords(data.facelessFormRecords);
        if (records.isEmpty()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.empty"));
            return 0;
        }
        deactivate(player, true);
        int selected = FacelessFormPolicy.normalizeSelection(
                records, data.facelessSelectedForm);
        data.facelessFormRecords = records;
        data.facelessSelectedForm = (selected + 1) % records.size();
        sendSelected(player, data);
        return 1;
    }

    public static int select(ServerPlayer player, int slot) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasFacelessAbility(data)) {
            unavailable(player);
            return 0;
        }
        List<CompoundTag> records =
                FacelessFormPolicy.normalizeRecords(data.facelessFormRecords);
        int index = slot - 1;
        if (index < 0 || index >= records.size()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.invalid_slot",
                    FacelessFormPolicy.MAX_FORMS));
            return 0;
        }
        deactivate(player, true);
        data.facelessFormRecords = records;
        data.facelessSelectedForm = index;
        sendSelected(player, data);
        return 1;
    }

    public static int clear(ServerPlayer player, int slot) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasFacelessAbility(data)) {
            unavailable(player);
            return 0;
        }
        List<CompoundTag> records = new java.util.ArrayList<>(
                FacelessFormPolicy.normalizeRecords(
                        data.facelessFormRecords));
        int index = slot - 1;
        if (index < 0 || index >= records.size()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.invalid_slot",
                    FacelessFormPolicy.MAX_FORMS));
            return 0;
        }
        deactivate(player, true);
        String removed = FacelessFormPolicy.displayName(records.remove(index));
        data.facelessFormRecords = List.copyOf(records);
        data.facelessSelectedForm =
                FacelessFormPolicy.normalizeSelection(records, index);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.cleared", removed));
        return 1;
    }

    public static int sendGuide(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasFacelessAbility(data)) {
            unavailable(player);
            return 0;
        }
        List<CompoundTag> records =
                FacelessFormPolicy.normalizeRecords(data.facelessFormRecords);
        data.facelessFormRecords = records;
        data.facelessSelectedForm = FacelessFormPolicy.normalizeSelection(
                records, data.facelessSelectedForm);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.status",
                records.size(), FacelessFormPolicy.MAX_FORMS,
                ACTIVE.containsKey(player.getUUID())
                        ? Component.translatable(
                        "message.lord_of_mysteries.faceless.state.active")
                        : Component.translatable(
                        "message.lord_of_mysteries.faceless.state.inactive")));
        for (int index = 0; index < records.size(); index++) {
            CompoundTag record = records.get(index);
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.entry",
                    index + 1,
                    index == data.facelessSelectedForm ? ">" : "-",
                    FacelessFormPolicy.displayName(record),
                    record.getString("entity_type")));
        }
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.controls"));
        return 1;
    }

    public static boolean isDisguised(Entity entity) {
        return entity != null && entity.getPersistentData().hasUUID(ACTIVE_FORM);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        tickPending(player);
        tickActive(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PENDING.remove(player.getUUID());
        deactivate(player, true);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PENDING.remove(player.getUUID());
        deactivate(player, true);
    }

    private static void tickPending(ServerPlayer player) {
        PendingRecord pending = PENDING.get(player.getUUID());
        if (pending == null) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasFacelessAbility(data)) {
            cancelRecording(player, "unavailable");
            return;
        }
        Entity entity = player.serverLevel().getEntity(pending.targetId());
        if (!(entity instanceof LivingEntity target)
                || !FacelessFormPolicy.canRecord(target)
                || !target.isAlive()
                || player.distanceToSqr(target) > RECORD_RANGE * RECORD_RANGE
                || !isLookingAt(player, target)) {
            cancelRecording(player, "lost");
            return;
        }
        long now = player.level().getGameTime();
        if (now < pending.completeAtTick()) return;
        if (!SpiritualityCost.tryConsume(data, RECORD_COST)) {
            PENDING.remove(player.getUUID());
            insufficient(player, RECORD_COST);
            return;
        }
        FacelessFormPolicy.Selection stored = FacelessFormPolicy.store(
                data.facelessFormRecords,
                FacelessFormPolicy.createRecord(target),
                data.facelessSelectedForm);
        data.facelessFormRecords = stored.records();
        data.facelessSelectedForm = stored.selectedIndex();
        data.facelessRecordCooldownEndTick =
                AbilityCooldowns.start(now, RECORD_COOLDOWN_TICKS);
        PENDING.remove(player.getUUID());
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.record_complete",
                target.getDisplayName(), stored.selectedIndex() + 1)
                .withStyle(ChatFormatting.AQUA));
        player.level().playSound(
                null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.65f, 1.35f);
    }

    private static void tickActive(ServerPlayer player) {
        ActiveDisguise active = ACTIVE.get(player.getUUID());
        if (active == null) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        List<CompoundTag> records =
                FacelessFormPolicy.normalizeRecords(data.facelessFormRecords);
        boolean recordStillExists = records.stream().anyMatch(record ->
                FacelessFormPolicy.recordId(record).equals(active.recordId()));
        if (!hasFacelessAbility(data) || !recordStillExists) {
            deactivate(player, false);
            return;
        }
        long now = player.level().getGameTime();
        if (now - active.lastDrainTick() < 20L) {
            applyDisguiseEffects(player);
            return;
        }
        if (!SpiritualityCost.tryConsume(
                data, TRANSFORM_DRAIN_PER_SECOND)) {
            deactivate(player, false);
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.exhausted"));
            return;
        }
        ACTIVE.put(player.getUUID(), new ActiveDisguise(
                active.recordId(), active.originalName(),
                active.originalNameVisible(), now));
        applyDisguiseEffects(player);
    }

    private static void applyDisguiseEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 45, 0, false, false, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 45, 0, false, false, true));
    }

    private static void deactivate(ServerPlayer player, boolean quiet) {
        ActiveDisguise active = ACTIVE.remove(player.getUUID());
        if (active == null) return;
        player.getPersistentData().remove(ACTIVE_FORM);
        player.getPersistentData().remove(ACTIVE_NAME);
        player.setCustomName(active.originalName());
        player.setCustomNameVisible(active.originalNameVisible());
        if (!quiet) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.faceless.deactivated"));
        }
    }

    private static boolean isLookingAt(
            ServerPlayer player, LivingEntity target) {
        Vec3 direction = target.getEyePosition()
                .subtract(player.getEyePosition());
        if (direction.lengthSqr() < 0.0001d) return true;
        return player.getLookAngle().normalize()
                .dot(direction.normalize()) >= 0.965d;
    }

    private static void cancelRecording(
            ServerPlayer player, String reason) {
        if (PENDING.remove(player.getUUID()) == null) return;
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.record_cancelled",
                Component.translatable(
                        "message.lord_of_mysteries.faceless.cancel."
                                + reason)));
    }

    private static void sendSelected(
            ServerPlayer player, PlayerMysteryData data) {
        CompoundTag record = data.facelessFormRecords.get(
                data.facelessSelectedForm);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.selected",
                data.facelessSelectedForm + 1,
                FacelessFormPolicy.displayName(record)));
    }

    private static boolean hasFacelessAbility(PlayerMysteryData data) {
        return SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                && data.sequence >= 0
                && data.sequence <= 6;
    }

    private static boolean unavailable(ServerPlayer player) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.faceless.unavailable"));
        return false;
    }

    private static boolean insufficient(
            ServerPlayer player, float cost) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.ability.insufficient_spirit", cost));
        return false;
    }

    private static boolean cooldown(
            ServerPlayer player, long cooldownEnd, long now) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.ability.cooldown",
                Math.max(1L,
                        AbilityCooldowns.remaining(cooldownEnd, now) / 20L)));
        return false;
    }

    private record PendingRecord(
            UUID targetId, long startedAtTick, long completeAtTick) {}

    private record ActiveDisguise(
            UUID recordId,
            Component originalName,
            boolean originalNameVisible,
            long lastDrainTick) {}
}
