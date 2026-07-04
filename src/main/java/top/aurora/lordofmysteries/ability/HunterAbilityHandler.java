package top.aurora.lordofmysteries.ability;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.HunterPotionItem;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HunterAbilityHandler {

    public static final String NATURAL_SPAWN_TAG =
            ProjectMystery.MOD_ID + ":natural_spawn";
    public static final String COMBAT_RECORD_TAG =
            ProjectMystery.MOD_ID + ":combat_record";
    public static final long TRACK_DURATION = 900L;
    public static final long TRACK_ACTING_THRESHOLD = 600L;
    public static final float PROVOKE_COST = 15f;
    public static final long PROVOKE_COOLDOWN = 400L;
    public static final long PROVOKE_DURATION = 160L;
    public static final float ENRAGE_COST = 20f;
    public static final long ENRAGE_COOLDOWN = 600L;
    public static final long ENRAGE_DURATION = 160L;
    public static final long BATTLE_WILL_COOLDOWN = 900L;
    public static final long BATTLE_WILL_DURATION = 200L;

    private static final String PROVOKE_OWNER_TAG =
            ProjectMystery.MOD_ID + ":provoke_owner";
    private static final String PROVOKE_END_TAG =
            ProjectMystery.MOD_ID + ":provoke_end";
    private static final String ENRAGE_END_TAG =
            ProjectMystery.MOD_ID + ":enrage_end";
    private static final UUID ENRAGE_ATTACK_UUID =
            UUID.fromString("7681f076-11a0-4f93-a42c-2796da9907ae");
    private static final UUID ENRAGE_ARMOR_UUID =
            UUID.fromString("81a0af5c-e113-4f02-807d-cc0c6ce50ac0");

    private HunterAbilityHandler() {}

    public static boolean provoke(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasHunterAbility(data, 8)) return unavailable(player);

        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.provokeCooldownEndTick, now)) {
            return cooldown(player, data.provokeCooldownEndTick, now);
        }
        AABB area = player.getBoundingBox().inflate(10d);
        List<Mob> targets = player.level().getEntitiesOfClass(Mob.class, area, mob ->
                mob.isAlive()
                        && (mob instanceof Enemy || mob.getTarget() != null)
                        && HunterAbilityLogic.canProvoke(mob.getMaxHealth(), false));
        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.hunter.no_provoke_targets"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, PROVOKE_COST)) {
            return insufficient(player, PROVOKE_COST);
        }

        data.provokeCooldownEndTick = AbilityCooldowns.start(now, PROVOKE_COOLDOWN);
        for (Mob target : targets) {
            target.setTarget(player);
            CompoundTag persistent = target.getPersistentData();
            persistent.putUUID(PROVOKE_OWNER_TAG, player.getUUID());
            persistent.putLong(PROVOKE_END_TAG, now + PROVOKE_DURATION);
        }
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                player.getX(), player.getY() + 1d, player.getZ(),
                20, 0.8, 0.6, 0.8, 0.02);
        level.playSound(null, player.blockPosition(),
                SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.7f, 1.25f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.hunter.provoke_applied",
                targets.size(), PROVOKE_DURATION / 20));
        if (data.sequence == 8 && targets.size() >= 3) {
            ActingEventHandler.trigger(player, ActingEvent.HUNTER8_PROVOKE_THREE, null);
        }
        return true;
    }

    public static boolean enrage(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasHunterAbility(data, 8)) return unavailable(player);

        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.enrageCooldownEndTick, now)) {
            return cooldown(player, data.enrageCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 12d);
        if (target == null) return noTarget(player);
        if (target instanceof ServerPlayer targetPlayer && !player.canHarmPlayer(targetPlayer)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.hunter.pvp_blocked"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, ENRAGE_COST)) {
            return insufficient(player, ENRAGE_COST);
        }

        data.enrageCooldownEndTick = AbilityCooldowns.start(now, ENRAGE_COOLDOWN);
        applyEnrage(target, now + ENRAGE_DURATION);
        player.serverLevel().sendParticles(ParticleTypes.FLAME,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                24, 0.4, 0.5, 0.4, 0.03);
        player.level().playSound(null, target.blockPosition(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 0.7f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.hunter.enrage_applied",
                target.getDisplayName(), ENRAGE_DURATION / 20));
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.hunter.enrage_received",
                    player.getDisplayName(), ENRAGE_DURATION / 20));
        }
        if (data.sequence == 8) {
            ActingEventHandler.trigger(player, ActingEvent.HUNTER8_ENRAGE, target);
        }
        return true;
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        MobSpawnType type = event.getSpawnType();
        if (type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION) {
            event.getEntity().getPersistentData().putBoolean(NATURAL_SPAWN_TAG, true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer hunter) {
            startTracking(hunter, target);
        }
        if (target instanceof ServerPlayer hunter
                && attacker instanceof LivingEntity livingAttacker) {
            livingAttacker.getPersistentData().putBoolean(COMBAT_RECORD_TAG, true);
            PlayerMysteryData data = MysteryCapability.get(hunter);
            if (hasHunterAbility(data, 8)
                    && data.battleWillEndTick > hunter.level().getGameTime()) {
                event.setAmount(event.getAmount() * 0.85f);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer hunter)) return;
        PlayerMysteryData data = MysteryCapability.get(hunter);
        if (!hasHunterAbility(data, 9)) return;
        LivingEntity target = event.getEntity();
        long now = hunter.level().getGameTime();
        if (!target.getUUID().toString().equals(data.hunterTrackedTarget)
                || now - data.hunterTrackingStartTick < TRACK_ACTING_THRESHOLD
                || !validHuntTarget(target)) {
            return;
        }
        if (data.sequence == 9) {
            ActingEventHandler.trigger(hunter, ActingEvent.HUNTER9_TRACK_AND_KILL, target);
        }
        clearTracking(data);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0) {
            return;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasHunterAbility(data, 9)) return;
        updateTracking(player, data);
        updateWildernessSense(player);
        updateBattleWill(player, data);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        long now = entity.level().getGameTime();
        CompoundTag persistent = entity.getPersistentData();
        if (entity instanceof Mob mob && persistent.hasUUID(PROVOKE_OWNER_TAG)) {
            long end = persistent.getLong(PROVOKE_END_TAG);
            if (now < end && entity.level() instanceof ServerLevel serverLevel) {
                ServerPlayer owner = serverLevel.getServer().getPlayerList()
                        .getPlayer(persistent.getUUID(PROVOKE_OWNER_TAG));
                if (owner != null && owner.level() == mob.level()) mob.setTarget(owner);
            } else {
                persistent.remove(PROVOKE_OWNER_TAG);
                persistent.remove(PROVOKE_END_TAG);
            }
        }
        if (persistent.contains(ENRAGE_END_TAG)
                && now >= persistent.getLong(ENRAGE_END_TAG)) {
            removeEnrage(entity);
        }
    }

    public static boolean hasHunterAbility(PlayerMysteryData data, int requiredSequence) {
        return HunterPotionItem.HUNTER_PATHWAY.equals(data.pathway)
                && data.sequence >= 0
                && data.sequence <= requiredSequence;
    }

    public static boolean validHuntTarget(LivingEntity target) {
        CompoundTag persistent = target.getPersistentData();
        return HunterAbilityLogic.validHuntTarget(
                persistent.getBoolean(NATURAL_SPAWN_TAG),
                target instanceof Enemy,
                persistent.getBoolean(COMBAT_RECORD_TAG));
    }

    private static void startTracking(ServerPlayer hunter, LivingEntity target) {
        PlayerMysteryData data = MysteryCapability.get(hunter);
        if (!hasHunterAbility(data, 9) || target == hunter) return;
        long now = hunter.level().getGameTime();
        String targetId = target.getUUID().toString();
        if (!targetId.equals(data.hunterTrackedTarget)
                || now >= data.hunterTrackingEndTick) {
            data.hunterTrackingStartTick = now;
            hunter.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.hunter.tracking_started",
                    target.getDisplayName()));
        }
        data.hunterTrackedTarget = targetId;
        data.hunterTrackingEndTick = now + TRACK_DURATION;
    }

    private static void updateTracking(ServerPlayer hunter, PlayerMysteryData data) {
        if (data.hunterTrackedTarget.isBlank()) return;
        long now = hunter.level().getGameTime();
        if (now >= data.hunterTrackingEndTick) {
            clearTracking(data);
            hunter.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.hunter.tracking_lost"));
            return;
        }
        UUID targetId;
        try {
            targetId = UUID.fromString(data.hunterTrackedTarget);
        } catch (IllegalArgumentException ignored) {
            clearTracking(data);
            return;
        }
        Entity entity = hunter.serverLevel().getEntity(targetId);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) return;

        DustParticleOptions trail = new DustParticleOptions(new Vector3f(0.65f, 0.08f, 0.04f), 1f);
        hunter.serverLevel().sendParticles(hunter, trail, true,
                target.getX(), target.getY() + 0.15, target.getZ(),
                5, 0.25, 0.08, 0.25, 0d);
        if (hunter.tickCount % 40 == 0) {
            hunter.displayClientMessage(Component.translatable(
                    "message.lord_of_mysteries.hunter.tracking_status",
                    target.getDisplayName(),
                    Math.round(Math.sqrt(hunter.distanceToSqr(target))),
                    Math.max(1L, (data.hunterTrackingEndTick - now) / 20L)), true);
        }
    }

    private static void updateWildernessSense(ServerPlayer hunter) {
        if (hunter.tickCount % 40 != 0
                || !hunter.serverLevel().canSeeSky(hunter.blockPosition())) {
            return;
        }
        AABB area = hunter.getBoundingBox().inflate(24d);
        hunter.level().getEntitiesOfClass(LivingEntity.class, area, target ->
                        target != hunter
                                && target.isAlive()
                                && validHuntTarget(target)
                                && (target instanceof Enemy
                                || target.getPersistentData().getBoolean(COMBAT_RECORD_TAG)))
                .stream()
                .min(Comparator.comparingDouble(hunter::distanceToSqr))
                .ifPresent(target -> {
                    DustParticleOptions hint =
                            new DustParticleOptions(new Vector3f(0.36f, 0.62f, 0.16f), 0.9f);
                    hunter.serverLevel().sendParticles(hunter, hint, true,
                            target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                            3, 0.15, 0.2, 0.15, 0d);
                });
    }

    private static void updateBattleWill(ServerPlayer hunter, PlayerMysteryData data) {
        if (!hasHunterAbility(data, 8)) return;
        long now = hunter.level().getGameTime();
        int attackers = hunter.level().getEntitiesOfClass(Mob.class,
                        hunter.getBoundingBox().inflate(16d),
                        mob -> mob.isAlive() && mob.getTarget() == hunter)
                .size();
        if (!HunterAbilityLogic.battleWillReady(
                attackers, data.battleWillCooldownEndTick, now)) {
            return;
        }
        data.battleWillEndTick = now + BATTLE_WILL_DURATION;
        data.battleWillCooldownEndTick = now + BATTLE_WILL_COOLDOWN;
        hunter.serverLevel().sendParticles(ParticleTypes.CRIT,
                hunter.getX(), hunter.getY() + 1d, hunter.getZ(),
                24, 0.5, 0.7, 0.5, 0.05);
        hunter.level().playSound(null, hunter.blockPosition(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 0.8f);
        hunter.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.hunter.battle_will",
                BATTLE_WILL_DURATION / 20));
        if (data.sequence == 8) {
            ActingEventHandler.trigger(hunter, ActingEvent.HUNTER8_BATTLE_WILL, null);
        }
    }

    private static void applyEnrage(LivingEntity target, long endTick) {
        removeEnrage(target);
        AttributeInstance attack = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.addTransientModifier(new AttributeModifier(
                    ENRAGE_ATTACK_UUID, "Hunter enrage attack",
                    0.30d, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.addTransientModifier(new AttributeModifier(
                    ENRAGE_ARMOR_UUID, "Hunter enrage armor",
                    -0.25d, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        target.getPersistentData().putLong(ENRAGE_END_TAG, endTick);
    }

    private static void removeEnrage(LivingEntity target) {
        AttributeInstance attack = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.removeModifier(ENRAGE_ATTACK_UUID);
        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(ENRAGE_ARMOR_UUID);
        target.getPersistentData().remove(ENRAGE_END_TAG);
    }

    private static void clearTracking(PlayerMysteryData data) {
        data.hunterTrackedTarget = "";
        data.hunterTrackingStartTick = 0L;
        data.hunterTrackingEndTick = 0L;
    }

    private static boolean unavailable(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.hunter.unavailable"));
        return false;
    }

    private static boolean noTarget(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.no_target"));
        return false;
    }

    private static boolean insufficient(ServerPlayer player, float cost) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.insufficient_spirit", cost));
        return false;
    }

    private static boolean cooldown(ServerPlayer player, long cooldownEnd, long now) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.cooldown",
                Math.max(1L, AbilityCooldowns.remaining(cooldownEnd, now) / 20L)));
        return false;
    }
}
