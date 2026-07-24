package top.aurora.lordofmysteries.ability;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.HunterPotionItem;
import top.aurora.lordofmysteries.potion.M2PathwayPotionItem;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.potion.SpectatorPotionItem;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class M3LaunchAbilityHandler {

    private static final String HARVEST_OWNER =
            ProjectMystery.MOD_ID + ":harvest_owner";
    private static final String HARVEST_EXPIRES =
            ProjectMystery.MOD_ID + ":harvest_expires";
    private static final String DREAM_STOLEN_DAY =
            ProjectMystery.MOD_ID + ":dream_stolen_day";

    private M3LaunchAbilityHandler() {}

    public static boolean use(
            ServerPlayer player, M2FoundationAbilityHandler.AbilitySlot slot) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.sequence == 6) return useSequenceSix(player, data, slot);
        if (data.sequence == 5) return useSequenceFive(player, data, slot);
        return false;
    }

    private static boolean useSequenceSix(
            ServerPlayer player, PlayerMysteryData data,
            M2FoundationAbilityHandler.AbilitySlot slot) {
        if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? facelessVeil(player, data) : facelessRestraint(player, data);
        }
        if (SpectatorPotionItem.SPECTATOR_PATHWAY.equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? hypnoticCommand(player, data) : mindBarrier(player, data);
        }
        if (HunterPotionItem.HUNTER_PATHWAY.equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? battlefieldLayout(player, data) : instigateConflict(player, data);
        }
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? stealBeneficialEffect(player, data) : retrieveUnownedItem(player, data);
        }
        if (M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? perfectCopy(player, data) : archiveFocus(player, data);
        }
        return false;
    }

    private static boolean useSequenceFive(
            ServerPlayer player, PlayerMysteryData data,
            M2FoundationAbilityHandler.AbilitySlot slot) {
        if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? revealSpiritThreads(player, data) : threadRestraint(player, data);
        }
        if (SpectatorPotionItem.SPECTATOR_PATHWAY.equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? lucidRehearsal(player, data) : dreamLull(player, data);
        }
        if (HunterPotionItem.HUNTER_PATHWAY.equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? flameScythe(player, data) : harvestMark(player, data);
        }
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? stealDream(player, data) : realityDislocation(player, data);
        }
        if (M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? travelerDoor(player, data) : returnToOutpost(player, data);
        }
        return false;
    }

    private static boolean facelessVeil(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.airBulletCooldownEndTick, now)) {
            return cooldown(player, data.airBulletCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, 25f)) return insufficient(player, 25f);
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 600, 0, false, true, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 600, 0, false, false, true));
        data.airBulletCooldownEndTick = AbilityCooldowns.start(now, 1200L);
        particles(player.serverLevel(), player.position(), ParticleTypes.CLOUD, 30);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.m3.faceless_veil")
                .withStyle(ChatFormatting.GRAY));
        ActingEventHandler.trigger(
                player, ActingEvent.FACELESS6_MAINTAIN_COVER, null);
        return true;
    }

    private static boolean facelessRestraint(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.stageIllusionCooldownEndTick, now)) {
            return cooldown(player, data.stageIllusionCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 10d);
        if (target == null
                || !M3LaunchAbilityLogic.canControl(
                        target instanceof Player, target.getMaxHealth())) {
            return invalidTarget(player);
        }
        if (!SpiritualityCost.tryConsume(data, 18f)) return insufficient(player, 18f);
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 200, 2, false, true));
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 200, 1, false, true));
        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 200, 0, false, true));
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        data.stageIllusionCooldownEndTick = AbilityCooldowns.start(now, 400L);
        particles(player.serverLevel(), target.position(), ParticleTypes.SMOKE, 24);
        success(player, "faceless_restraint", target.getDisplayName());
        ActingEventHandler.trigger(
                player, ActingEvent.FACELESS6_RESTRAIN_WITHOUT_IDENTITY, target);
        return true;
    }

    private static boolean revealSpiritThreads(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.airBulletCooldownEndTick, now)) {
            return cooldown(player, data.airBulletCooldownEndTick, now);
        }
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(18d),
                target -> target != player && target.isAlive()
                        && !(target instanceof Player));
        if (targets.isEmpty()) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, 35f)) return insufficient(player, 35f);
        targets.forEach(target -> {
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, 240, 0, false, true));
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false));
        });
        data.airBulletCooldownEndTick = AbilityCooldowns.start(now, 600L);
        particles(player.serverLevel(), player.position(), ParticleTypes.END_ROD, 56);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.m3.spirit_threads", targets.size()));
        ActingEventHandler.trigger(
                player, ActingEvent.MARIONETTIST5_REVEAL_THREADS, null);
        return true;
    }

    private static boolean threadRestraint(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.stageIllusionCooldownEndTick, now)) {
            return cooldown(player, data.stageIllusionCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 14d);
        if (target == null
                || !M3LaunchAbilityLogic.canRestrain(
                        target instanceof Player, target.getMaxHealth(), target.getHealth())) {
            return invalidTarget(player);
        }
        if (!SpiritualityCost.tryConsume(data, 30f)) return insufficient(player, 30f);
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 180, 4, false, true));
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 180, 3, false, true));
        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 180, 0, false, true));
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        data.stageIllusionCooldownEndTick = AbilityCooldowns.start(now, 500L);
        success(player, "thread_restraint", target.getDisplayName());
        ActingEventHandler.trigger(
                player, ActingEvent.MARIONETTIST5_RESTRAIN_CRISIS, target);
        return true;
    }

    private static boolean hypnoticCommand(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.psychPacifyCooldownEndTick, now)) {
            return cooldown(player, data.psychPacifyCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 14d);
        if (!(target instanceof Mob mob)
                || !M3LaunchAbilityLogic.canControl(false, mob.getMaxHealth())) {
            return invalidTarget(player);
        }
        if (!SpiritualityCost.tryConsume(data, 30f)) return insufficient(player, 30f);
        boolean hostile = mob.getTarget() != null;
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 240, 2, false, true));
        mob.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 240, 2, false, true));
        data.psychPacifyCooldownEndTick = AbilityCooldowns.start(now, 900L);
        particles(player.serverLevel(), mob.position(), ParticleTypes.ENCHANT, 34);
        success(player, "hypnotic_command", mob.getDisplayName());
        if (hostile) {
            ActingEventHandler.trigger(
                    player, ActingEvent.HYPNOTIST6_DEESCALATE_HOSTILE, mob);
        }
        return true;
    }

    private static boolean mindBarrier(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.psychShockCooldownEndTick, now)) {
            return cooldown(player, data.psychShockCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, 25f)) return insufficient(player, 25f);
        List<ServerPlayer> protectedPlayers = player.level().getEntitiesOfClass(
                ServerPlayer.class, player.getBoundingBox().inflate(6d), Entity::isAlive);
        protectedPlayers.forEach(target -> {
            target.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 400, 0, false, true, true));
            target.addEffect(new MobEffectInstance(
                    MobEffects.ABSORPTION, 400, 0, false, true, true));
            target.removeEffect(MobEffects.CONFUSION);
            target.removeEffect(MobEffects.DARKNESS);
        });
        data.psychShockCooldownEndTick = AbilityCooldowns.start(now, 1200L);
        particles(player.serverLevel(), player.position(), ParticleTypes.ENCHANT, 48);
        success(player, "mind_barrier", protectedPlayers.size());
        if (protectedPlayers.size() > 1) {
            ActingEventHandler.trigger(
                    player, ActingEvent.HYPNOTIST6_BARRIER_ALLY, null);
        }
        return true;
    }

    private static boolean lucidRehearsal(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.psychPacifyCooldownEndTick, now)) {
            return cooldown(player, data.psychPacifyCooldownEndTick, now);
        }
        if (data.insanityPressure <= 0f && data.mentalTraumaEndTick <= now) {
            return invalidTarget(player);
        }
        if (!SpiritualityCost.tryConsume(data, 40f)) return insufficient(player, 40f);
        data.insanityPressure = Math.max(0f, data.insanityPressure - 12f);
        data.mentalTraumaEndTick = 0L;
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DARKNESS);
        data.psychPacifyCooldownEndTick = AbilityCooldowns.start(now, 12000L);
        particles(player.serverLevel(), player.position(), ParticleTypes.PORTAL, 42);
        success(player, "lucid_rehearsal",
                String.format(java.util.Locale.ROOT, "%.1f", data.insanityPressure));
        ActingEventHandler.trigger(
                player, ActingEvent.DREAMWALKER5_LUCID_RECOVERY, null);
        return true;
    }

    private static boolean dreamLull(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.psychShockCooldownEndTick, now)) {
            return cooldown(player, data.psychShockCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 16d);
        if (!(target instanceof Mob mob)
                || !M3LaunchAbilityLogic.canControl(false, mob.getMaxHealth())) {
            return invalidTarget(player);
        }
        if (!SpiritualityCost.tryConsume(data, 30f)) return insufficient(player, 30f);
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 300, 4, false, true));
        mob.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 300, 3, false, true));
        mob.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION, 300, 0, false, true));
        data.psychShockCooldownEndTick = AbilityCooldowns.start(now, 800L);
        particles(player.serverLevel(), mob.position(), ParticleTypes.PORTAL, 38);
        success(player, "dream_lull", mob.getDisplayName());
        ActingEventHandler.trigger(
                player, ActingEvent.DREAMWALKER5_LULL_THREAT, mob);
        return true;
    }

    private static boolean battlefieldLayout(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.pyroSpearCooldownEndTick, now)) {
            return cooldown(player, data.pyroSpearCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, 30f)) return insufficient(player, 30f);
        List<Mob> threats = monstersAround(player, 16d);
        threats.forEach(target -> {
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, 600, 0, false, true));
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 300, 0, false, false));
        });
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST, 600, 0, false, true, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 600, 0, false, true, true));
        data.pyroSpearCooldownEndTick = AbilityCooldowns.start(now, 3600L);
        particles(player.serverLevel(), player.position(), ParticleTypes.FLAME, 64);
        success(player, "battlefield_layout", threats.size());
        ActingEventHandler.trigger(
                player, ActingEvent.CONSPIRER6_PREPARE_BATTLEFIELD, null);
        return true;
    }

    private static boolean instigateConflict(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.pyroRingCooldownEndTick, now)) {
            return cooldown(player, data.pyroRingCooldownEndTick, now);
        }
        LivingEntity looked = AbilityTargeting.findLookTarget(player, 14d);
        if (!(looked instanceof Mob first)
                || !M3LaunchAbilityLogic.canControl(false, first.getMaxHealth())) {
            return invalidTarget(player);
        }
        Mob second = player.level().getEntitiesOfClass(
                        Mob.class, first.getBoundingBox().inflate(8d),
                        mob -> mob != first && mob.isAlive()
                                && mob.getType().getCategory() == MobCategory.MONSTER
                                && M3LaunchAbilityLogic.canControl(
                                        false, mob.getMaxHealth()))
                .stream()
                .min(Comparator.comparingDouble(first::distanceToSqr))
                .orElse(null);
        if (second == null) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, 25f)) return insufficient(player, 25f);
        first.setTarget(second);
        second.setTarget(first);
        first.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 300, 0, false, true));
        second.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 300, 0, false, true));
        data.pyroRingCooldownEndTick = AbilityCooldowns.start(now, 1200L);
        particles(player.serverLevel(), first.position(), ParticleTypes.ANGRY_VILLAGER, 18);
        particles(player.serverLevel(), second.position(), ParticleTypes.ANGRY_VILLAGER, 18);
        success(player, "instigate_conflict",
                first.getDisplayName(), second.getDisplayName());
        ActingEventHandler.trigger(
                player, ActingEvent.CONSPIRER6_TURN_ENEMIES, first);
        return true;
    }

    private static boolean flameScythe(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.pyroSpearCooldownEndTick, now)) {
            return cooldown(player, data.pyroSpearCooldownEndTick, now);
        }
        Vec3 look = player.getLookAngle().normalize();
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(5d),
                target -> target != player && target.isAlive()
                        && (target.getType().getCategory() == MobCategory.MONSTER
                            || target instanceof ServerPlayer targetPlayer
                            && player.canHarmPlayer(targetPlayer))
                        && look.dot(target.position()
                                .subtract(player.position()).normalize()) > 0.1d);
        if (targets.isEmpty()) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, 35f)) return insufficient(player, 35f);
        targets.forEach(target -> {
            target.hurt(player.damageSources().playerAttack(player), 8f);
            target.setSecondsOnFire(6);
            target.knockback(0.8d,
                    player.getX() - target.getX(), player.getZ() - target.getZ());
        });
        data.pyroSpearCooldownEndTick = AbilityCooldowns.start(now, 500L);
        particles(player.serverLevel(), player.position(), ParticleTypes.FLAME, 72);
        success(player, "flame_scythe", targets.size());
        ActingEventHandler.trigger(
                player, ActingEvent.REAPER5_SWEEP_FLAMES, null);
        return true;
    }

    private static boolean harvestMark(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.pyroRingCooldownEndTick, now)) {
            return cooldown(player, data.pyroRingCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 18d);
        if (target == null || target instanceof Player || target.getMaxHealth() > 120f) {
            return invalidTarget(player);
        }
        if (!SpiritualityCost.tryConsume(data, 20f)) return insufficient(player, 20f);
        target.getPersistentData().putUUID(HARVEST_OWNER, player.getUUID());
        target.getPersistentData().putLong(HARVEST_EXPIRES, now + 1200L);
        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 1200, 0, false, true));
        data.pyroRingCooldownEndTick = AbilityCooldowns.start(now, 300L);
        particles(player.serverLevel(), target.position(), ParticleTypes.FLAME, 28);
        success(player, "harvest_mark", target.getDisplayName());
        return true;
    }

    private static boolean stealBeneficialEffect(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefRuneCooldownEndTick, now)) {
            return cooldown(player, data.thiefRuneCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 10d);
        if (target == null
                || !M3LaunchAbilityLogic.canControl(
                        target instanceof Player, target.getMaxHealth())) {
            return invalidTarget(player);
        }
        MobEffectInstance stolen = target.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().isBeneficial())
                .max(Comparator.comparingInt(MobEffectInstance::getDuration))
                .orElse(null);
        if (stolen == null) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, 45f)) return insufficient(player, 45f);
        target.removeEffect(stolen.getEffect());
        player.addEffect(new MobEffectInstance(
                stolen.getEffect(), Math.min(1200, stolen.getDuration()),
                stolen.getAmplifier(), false, true, true));
        if (player.getRandom().nextFloat() < 0.25f) {
            data.insanityPressure = Math.min(100f, data.insanityPressure + 15f);
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.m3.theft_backlash")
                    .withStyle(ChatFormatting.RED));
        }
        data.thiefRuneCooldownEndTick = AbilityCooldowns.start(now, 4800L);
        particles(player.serverLevel(), target.position(), ParticleTypes.WITCH, 34);
        success(player, "effect_theft", stolen.getEffect().getDisplayName());
        ActingEventHandler.trigger(
                player, ActingEvent.PROMETHEUS6_STEAL_POWER, target);
        return true;
    }

    private static boolean retrieveUnownedItem(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefLockpickCooldownEndTick, now)) {
            return cooldown(player, data.thiefLockpickCooldownEndTick, now);
        }
        ItemEntity item = player.level().getEntitiesOfClass(
                        ItemEntity.class, player.getBoundingBox().inflate(10d),
                        candidate -> {
                            Entity owner = candidate.getOwner();
                            return candidate.isAlive()
                                    && M3LaunchAbilityLogic.canRetrieveItem(
                                            owner != null,
                                            owner == player);
                        })
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (item == null) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, 12f)) return insufficient(player, 12f);
        item.teleportTo(player.getX(), player.getY() + 0.5d, player.getZ());
        item.setNoPickUpDelay();
        data.thiefLockpickCooldownEndTick = AbilityCooldowns.start(now, 200L);
        particles(player.serverLevel(), player.position(), ParticleTypes.PORTAL, 24);
        success(player, "unowned_retrieval", item.getItem().getHoverName());
        ActingEventHandler.trigger(
                player, ActingEvent.PROMETHEUS6_RECOVER_UNOWNED, item);
        return true;
    }

    private static boolean stealDream(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefRuneCooldownEndTick, now)) {
            return cooldown(player, data.thiefRuneCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 8d);
        if (!(target instanceof Villager villager) || !villager.isSleeping()) {
            return invalidTarget(player);
        }
        long day = player.level().getDayTime() / 24000L;
        if (villager.getPersistentData().contains(DREAM_STOLEN_DAY)
                && villager.getPersistentData().getLong(DREAM_STOLEN_DAY) == day) {
            return invalidTarget(player);
        }
        if (!SpiritualityCost.tryConsume(data, 40f)) return insufficient(player, 40f);
        villager.getPersistentData().putLong(DREAM_STOLEN_DAY, day);
        ItemStack fragment = new ItemStack(ModItems.DREAM_SCALE_FRAGMENT.get());
        if (!player.getInventory().add(fragment)) player.drop(fragment, false);
        data.thiefRuneCooldownEndTick = AbilityCooldowns.start(now, 2400L);
        particles(player.serverLevel(), villager.position(), ParticleTypes.PORTAL, 32);
        success(player, "dream_theft");
        ActingEventHandler.trigger(
                player, ActingEvent.DREAM_STEALER5_RETRIEVE_DREAM, villager);
        return true;
    }

    private static boolean realityDislocation(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefLockpickCooldownEndTick, now)) {
            return cooldown(player, data.thiefLockpickCooldownEndTick, now);
        }
        Vec3 desired = player.position().add(player.getLookAngle().scale(6d));
        Vec3 destination = findSafeDestination(player.serverLevel(), player, desired);
        if (destination == null) return unsafeDestination(player);
        if (!SpiritualityCost.tryConsume(data, 35f)) return insufficient(player, 35f);
        Vec3 origin = player.position();
        player.teleportTo(destination.x, destination.y, destination.z);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 60, 1, false, false, true));
        data.thiefLockpickCooldownEndTick = AbilityCooldowns.start(now, 900L);
        particles(player.serverLevel(), origin, ParticleTypes.PORTAL, 28);
        particles(player.serverLevel(), destination, ParticleTypes.PORTAL, 28);
        success(player, "reality_dislocation");
        ActingEventHandler.trigger(
                player, ActingEvent.DREAM_STEALER5_ESCAPE_REALITY, null);
        return true;
    }

    private static boolean perfectCopy(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeRelocateCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeRelocateCooldownEndTick, now);
        }
        ItemStack source = player.getMainHandItem();
        if (!source.is(Items.WRITTEN_BOOK) && !source.is(Items.FILLED_MAP)) {
            return invalidTarget(player);
        }
        ItemStack copy = source.copy();
        copy.setCount(1);
        if (source.is(Items.WRITTEN_BOOK)) {
            int generation = source.hasTag()
                    ? source.getTag().getInt("generation") : 0;
            int nextGeneration =
                    M3LaunchAbilityLogic.copiedBookGeneration(generation);
            if (nextGeneration < 0) return invalidTarget(player);
            copy.getOrCreateTag().putInt("generation", nextGeneration);
        }
        if (!SpiritualityCost.tryConsume(data, 25f)) return insufficient(player, 25f);
        if (!player.getInventory().add(copy)) player.drop(copy, false);
        data.apprenticeRelocateCooldownEndTick =
                AbilityCooldowns.start(now, 2400L);
        particles(player.serverLevel(), player.position(), ParticleTypes.ENCHANT, 36);
        success(player, "perfect_copy", copy.getHoverName());
        ActingEventHandler.trigger(
                player, ActingEvent.SCRIBE6_COPY_KNOWLEDGE, null);
        return true;
    }

    private static boolean archiveFocus(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeWardCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeWardCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, 20f)) return insufficient(player, 20f);
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, 1200, 0, false, false, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.DIG_SPEED, 1200, 1, false, false, true));
        data.apprenticeWardCooldownEndTick =
                AbilityCooldowns.start(now, 1200L);
        particles(player.serverLevel(), player.position(), ParticleTypes.ENCHANT, 30);
        success(player, "archive_focus");
        ActingEventHandler.trigger(
                player, ActingEvent.SCRIBE6_ARCHIVE_FOCUS, null);
        return true;
    }

    private static boolean travelerDoor(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeRelocateCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeRelocateCooldownEndTick, now);
        }
        HitResult hit = player.pick(14d, 0f, false);
        if (!(hit instanceof BlockHitResult blockHit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return noTarget(player);
        }
        Direction direction = blockHit.getDirection();
        BlockPos desiredBlock = blockHit.getBlockPos().relative(direction);
        Vec3 destination = findSafeDestination(
                player.serverLevel(), player, Vec3.atBottomCenterOf(desiredBlock));
        if (destination == null) return unsafeDestination(player);
        if (!SpiritualityCost.tryConsume(data, 20f)) return insufficient(player, 20f);
        Vec3 origin = player.position();
        player.teleportTo(destination.x, destination.y, destination.z);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 60, 1, false, false, true));
        data.apprenticeRelocateCooldownEndTick =
                AbilityCooldowns.start(now, 120L);
        particles(player.serverLevel(), origin, ParticleTypes.PORTAL, 36);
        particles(player.serverLevel(), destination, ParticleTypes.PORTAL, 36);
        success(player, "traveler_door");
        ActingEventHandler.trigger(
                player, ActingEvent.TRAVELER5_CROSS_DISTANCE, null);
        return true;
    }

    private static boolean returnToOutpost(
            ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeWardCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeWardCooldownEndTick, now);
        }
        if (player.level().dimension() != Level.OVERWORLD) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.m3.overworld_only"));
            return false;
        }
        BlockPos outpost = MistCityOutpostSavedData.get(player.serverLevel())
                .outpost().orElse(null);
        if (outpost == null) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.m3.no_outpost"));
            return false;
        }
        player.serverLevel().getChunkAt(outpost);
        Vec3 destination = findSafeDestination(
                player.serverLevel(), player, Vec3.atBottomCenterOf(outpost.above()));
        if (destination == null) return unsafeDestination(player);
        if (!SpiritualityCost.tryConsume(data, 80f)) return insufficient(player, 80f);
        Vec3 origin = player.position();
        player.teleportTo(destination.x, destination.y, destination.z);
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 100, 2, false, false, true));
        data.apprenticeWardCooldownEndTick =
                AbilityCooldowns.start(now, 36000L);
        particles(player.serverLevel(), origin, ParticleTypes.END_ROD, 48);
        particles(player.serverLevel(), destination, ParticleTypes.END_ROD, 48);
        success(player, "outpost_return");
        ActingEventHandler.trigger(
                player, ActingEvent.TRAVELER5_RETURN_OUTPOST, null);
        return true;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)
                || !target.getPersistentData().hasUUID(HARVEST_OWNER)
                || target.getPersistentData().getLong(HARVEST_EXPIRES)
                    < level.getGameTime()) {
            return;
        }
        UUID ownerId = target.getPersistentData().getUUID(HARVEST_OWNER);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) return;
        PlayerMysteryData data = MysteryCapability.get(owner);
        if (!HunterPotionItem.HUNTER_PATHWAY.equals(data.pathway)
                || data.sequence != 5) {
            return;
        }
        SpiritualityCost.refund(data, 15f);
        data.pyroSpearCooldownEndTick = level.getGameTime();
        PlayerFeedback.send(owner, Component.translatable(
                "message.lord_of_mysteries.m3.harvest_refund")
                .withStyle(ChatFormatting.GOLD));
        ActingEventHandler.trigger(
                owner, ActingEvent.REAPER5_HARVEST_PREY, target);
    }

    private static List<Mob> monstersAround(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(radius),
                target -> target.isAlive()
                        && target.getType().getCategory() == MobCategory.MONSTER);
    }

    private static Vec3 findSafeDestination(
            ServerLevel level, Entity entity, Vec3 desired) {
        for (int yOffset = 0; yOffset <= 3; yOffset++) {
            BlockPos feet = BlockPos.containing(desired).above(yOffset);
            if (!level.getBlockState(feet.below()).isFaceSturdy(
                    level, feet.below(), Direction.UP)) {
                continue;
            }
            Vec3 candidate = Vec3.atBottomCenterOf(feet);
            Vec3 move = candidate.subtract(entity.position());
            if (level.noCollision(entity, entity.getBoundingBox().move(move))) {
                return candidate;
            }
        }
        return null;
    }

    private static void particles(
            ServerLevel level, Vec3 position,
            net.minecraft.core.particles.SimpleParticleType type, int count) {
        level.sendParticles(type, position.x, position.y + 0.7d, position.z,
                count, 0.5d, 0.7d, 0.5d, 0.04d);
    }

    private static boolean noTarget(ServerPlayer player) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.ability.no_target"));
        return false;
    }

    private static void success(
            ServerPlayer player, String action, Object... arguments) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.m3." + action, arguments)
                .withStyle(ChatFormatting.AQUA));
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 0.55f, 1.2f);
    }

    private static boolean invalidTarget(ServerPlayer player) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.m3.invalid_target"));
        return false;
    }

    private static boolean unsafeDestination(ServerPlayer player) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.m3.unsafe_destination"));
        return false;
    }

    private static boolean insufficient(ServerPlayer player, float cost) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.ability.insufficient_spirit", cost));
        return false;
    }

    private static boolean cooldown(
            ServerPlayer player, long cooldownEnd, long now) {
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.ability.cooldown",
                Math.max(1L, AbilityCooldowns.remaining(cooldownEnd, now) / 20L)));
        return false;
    }
}
