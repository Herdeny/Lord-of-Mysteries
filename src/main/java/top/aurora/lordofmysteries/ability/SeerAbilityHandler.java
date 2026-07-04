package top.aurora.lordofmysteries.ability;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SeerAbilityHandler {

    public enum Ability {
        CARD_BLADE,
        FLAME_LEAP,
        PAPER_SUBSTITUTE,
        AIR_BULLET,
        STAGE_ILLUSION
    }

    public static final float CARD_BLADE_COST = 12f;
    public static final long CARD_BLADE_COOLDOWN = 10L;
    public static final float FLAME_LEAP_COST = 18f;
    public static final long FLAME_LEAP_COOLDOWN = 120L;
    public static final float PAPER_SUBSTITUTE_COST = 25f;
    public static final long PAPER_SUBSTITUTE_COOLDOWN = 900L;
    public static final long PAPER_SUBSTITUTE_DURATION = 600L;
    public static final float AIR_BULLET_COST = 6f;
    public static final long AIR_BULLET_COOLDOWN = 20L;
    public static final float STAGE_ILLUSION_COST = 20f;
    public static final long STAGE_ILLUSION_COOLDOWN = 600L;
    public static final long STAGE_ILLUSION_DURATION = 200L;
    public static final long DODGE_COOLDOWN = 160L;
    public static final float EXPRESSION_CONTROL_COST = 8f;
    public static final long EXPRESSION_CONTROL_COOLDOWN = 200L;

    private static final String ILLUSION_END_TAG =
            ProjectMystery.MOD_ID + ":stage_illusion_end";
    private static final String ILLUSION_X_TAG =
            ProjectMystery.MOD_ID + ":stage_illusion_x";
    private static final String ILLUSION_Y_TAG =
            ProjectMystery.MOD_ID + ":stage_illusion_y";
    private static final String ILLUSION_Z_TAG =
            ProjectMystery.MOD_ID + ":stage_illusion_z";

    private SeerAbilityHandler() {}

    public static boolean use(ServerPlayer player, Ability ability) {
        return switch (ability) {
            case CARD_BLADE -> cardBlade(player);
            case FLAME_LEAP -> flameLeap(player);
            case PAPER_SUBSTITUTE -> armPaperSubstitute(player);
            case AIR_BULLET -> airBullet(player);
            case STAGE_ILLUSION -> stageIllusion(player);
        };
    }

    public static boolean cardBlade(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSeerAbility(data, 8)) return unavailable(player, 8);
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.cardBladeCooldownEndTick, now)) {
            return cooldown(player, data.cardBladeCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 16d);
        if (target == null) return noTarget(player);
        if (!canHarm(player, target)) return pvpBlocked(player);
        if (!hasItem(player, ModItems.MYSTIC_PLAYING_CARDS.get(), 3)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.seer.cards_required"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, CARD_BLADE_COST)) {
            return insufficient(player, CARD_BLADE_COST);
        }

        boolean spiritBurning = consumeOptional(player, Items.FIRE_CHARGE, 1);
        consumeItem(player, ModItems.MYSTIC_PLAYING_CARDS.get(), 3);
        data.cardBladeCooldownEndTick = AbilityCooldowns.start(now, CARD_BLADE_COOLDOWN);
        target.hurt(player.damageSources().playerAttack(player),
                SeerAbilityLogic.cardVolleyDamage(spiritBurning));
        if (spiritBurning) target.setSecondsOnFire(4);
        player.serverLevel().sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() * 0.55d, target.getZ(),
                18, 0.35d, 0.45d, 0.35d, 0.12d);
        player.level().playSound(null, target.blockPosition(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.7f, 1.55f);
        player.sendSystemMessage(Component.translatable(
                spiritBurning
                        ? "message.lord_of_mysteries.seer.card_blade_burning"
                        : "message.lord_of_mysteries.seer.card_blade",
                target.getDisplayName()));

        int audience = player.serverLevel().getPlayers(other ->
                other != player && other.distanceToSqr(player) <= 144d).size();
        if (data.sequence == 8 && audience >= 2) {
            ActingEventHandler.trigger(player, ActingEvent.CLOWN8_PERFORMANCE, target);
        }
        return true;
    }

    public static boolean flameLeap(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSeerAbility(data, 7)) return unavailable(player, 7);
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.flameLeapCooldownEndTick, now)) {
            return cooldown(player, data.flameLeapCooldownEndTick, now);
        }
        Vec3 destination = findSafeLeapDestination(player);
        if (destination == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.seer.no_safe_destination"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, FLAME_LEAP_COST)) {
            return insufficient(player, FLAME_LEAP_COST);
        }

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.FLAME,
                player.getX(), player.getY() + 0.8d, player.getZ(),
                28, 0.35d, 0.65d, 0.35d, 0.08d);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.fallDistance = 0f;
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 0.7d, player.getZ(),
                36, 0.8d, 0.35d, 0.8d, 0.05d);
        level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(2.5d),
                mob -> mob.isAlive() && (mob instanceof Enemy || mob.getTarget() != null))
                .forEach(mob -> mob.setSecondsOnFire(3));
        level.playSound(null, player.blockPosition(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9f, 1.35f);
        data.flameLeapCooldownEndTick = AbilityCooldowns.start(now, FLAME_LEAP_COOLDOWN);
        data.flameLeapStrikeEndTick = now + 100L;
        return true;
    }

    public static boolean armPaperSubstitute(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSeerAbility(data, 7)) return unavailable(player, 7);
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.paperSubstituteCooldownEndTick, now)) {
            return cooldown(player, data.paperSubstituteCooldownEndTick, now);
        }
        if (!hasItem(player, ModItems.PAPER_FIGURINE.get(), 1)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.seer.figurine_required"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, PAPER_SUBSTITUTE_COST)) {
            return insufficient(player, PAPER_SUBSTITUTE_COST);
        }

        consumeItem(player, ModItems.PAPER_FIGURINE.get(), 1);
        data.paperSubstituteCooldownEndTick =
                AbilityCooldowns.start(now, PAPER_SUBSTITUTE_COOLDOWN);
        data.paperSubstituteArmedEndTick = now + PAPER_SUBSTITUTE_DURATION;
        data.paperSubstituteDimension = player.level().dimension().location().toString();
        data.paperSubstituteX = player.getX();
        data.paperSubstituteY = player.getY();
        data.paperSubstituteZ = player.getZ();
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.7f, 1.5f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.seer.substitute_armed",
                PAPER_SUBSTITUTE_DURATION / 20));
        return true;
    }

    public static boolean airBullet(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSeerAbility(data, 7)) return unavailable(player, 7);
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.airBulletCooldownEndTick, now)) {
            return cooldown(player, data.airBulletCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 20d);
        if (target == null) return noTarget(player);
        if (!canHarm(player, target)) return pvpBlocked(player);
        if (!SpiritualityCost.tryConsume(data, AIR_BULLET_COST)) {
            return insufficient(player, AIR_BULLET_COST);
        }

        data.airBulletCooldownEndTick = AbilityCooldowns.start(now, AIR_BULLET_COOLDOWN);
        target.hurt(player.damageSources().playerAttack(player), 4f);
        target.knockback(0.35d, player.getX() - target.getX(), player.getZ() - target.getZ());
        player.serverLevel().sendParticles(ParticleTypes.CLOUD,
                target.getX(), target.getY() + target.getBbHeight() * 0.5d, target.getZ(),
                12, 0.18d, 0.25d, 0.18d, 0.08d);
        player.level().playSound(null, target.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.45f, 1.8f);
        if (data.sequence == 7 && now <= data.flameLeapStrikeEndTick) {
            data.flameLeapStrikeEndTick = 0L;
            ActingEventHandler.trigger(player, ActingEvent.MAGICIAN7_FLASHY_ENTRY, target);
        }
        return true;
    }

    public static boolean stageIllusion(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSeerAbility(data, 7)) return unavailable(player, 7);
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.stageIllusionCooldownEndTick, now)) {
            return cooldown(player, data.stageIllusionCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, STAGE_ILLUSION_COST)) {
            return insufficient(player, STAGE_ILLUSION_COST);
        }

        Vec3 point = player.position().add(player.getLookAngle().scale(6d));
        List<Mob> affected = player.level().getEntitiesOfClass(
                Mob.class, new AABB(point, point).inflate(6d),
                mob -> mob.isAlive() && (mob instanceof Enemy || mob.getTarget() != null));
        data.stageIllusionCooldownEndTick = AbilityCooldowns.start(now, STAGE_ILLUSION_COOLDOWN);
        for (Mob mob : affected) {
            mob.setTarget(null);
            mob.getPersistentData().putLong(ILLUSION_END_TAG, now + STAGE_ILLUSION_DURATION);
            mob.getPersistentData().putDouble(ILLUSION_X_TAG, point.x);
            mob.getPersistentData().putDouble(ILLUSION_Y_TAG, point.y);
            mob.getPersistentData().putDouble(ILLUSION_Z_TAG, point.z);
            mob.getNavigation().moveTo(point.x, point.y, point.z, 1.15d);
        }
        player.serverLevel().sendParticles(ParticleTypes.WITCH,
                point.x, point.y + 1d, point.z,
                54, 1.5d, 1d, 1.5d, 0.02d);
        player.level().playSound(null, BlockPos.containing(point),
                SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.8f, 1.2f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.seer.illusion_applied", affected.size()));
        if (data.sequence == 7 && affected.size() >= 3) {
            ActingEventHandler.trigger(player, ActingEvent.MAGICIAN7_DECEIVE_MOB, null);
        }
        return true;
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSeerAbility(data, 8)) return;
        Entity direct = event.getSource().getDirectEntity();
        boolean melee = direct instanceof LivingEntity && direct == event.getSource().getEntity();
        long now = player.level().getGameTime();
        if (!SeerAbilityLogic.intuitiveDodge(
                player.getRandom().nextFloat(), melee, data.clownDodgeCooldownEndTick, now)) {
            return;
        }

        event.setCanceled(true);
        data.clownDodgeCooldownEndTick = AbilityCooldowns.start(now, DODGE_COOLDOWN);
        data.clownDodgeCount++;
        player.serverLevel().sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.8d, player.getZ(),
                18, 0.4d, 0.7d, 0.4d, 0.06d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.8f, 1.7f);
        player.displayClientMessage(Component.translatable(
                "message.lord_of_mysteries.seer.intuitive_dodge")
                .withStyle(ChatFormatting.AQUA), true);
        if (data.sequence == 8 && data.clownDodgeCount >= 5) {
            data.clownDodgeCount = 0;
            ActingEventHandler.trigger(player, ActingEvent.CLOWN8_DODGE_MASTER, null);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        long now = player.level().getGameTime();
        if (!hasSeerAbility(data, 7)
                || !SeerAbilityLogic.paperSubstituteTriggers(
                event.getAmount(), player.getHealth(), data.paperSubstituteArmedEndTick, now)) {
            return;
        }

        event.setCanceled(true);
        if (data.paperSubstituteDimension.equals(
                player.level().dimension().location().toString())) {
            player.teleportTo(
                    data.paperSubstituteX, data.paperSubstituteY, data.paperSubstituteZ);
        }
        data.paperSubstituteArmedEndTick = 0L;
        data.paperSubstituteDimension = "";
        player.setHealth(Math.max(1f, player.getHealth()));
        player.invulnerableTime = 30;
        player.serverLevel().sendParticles(ParticleTypes.POOF,
                player.getX(), player.getY() + 0.8d, player.getZ(),
                36, 0.5d, 0.7d, 0.5d, 0.08d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.65f, 1.6f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.seer.substitute_triggered"));
        if (data.sequence == 7) {
            ActingEventHandler.trigger(player, ActingEvent.MAGICIAN7_GRAND_ESCAPE, null);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!hasSeerAbility(MysteryCapability.get(player), 8)) return;
        event.setDistance(Math.max(0f, event.getDistance() - 4f));
        event.setDamageMultiplier(event.getDamageMultiplier() * 0.5f);
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!hasSeerAbility(MysteryCapability.get(player), 8)) return;
        event.setStrength(event.getStrength() * 0.5f);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.sequence == 8
                && SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                && data.insanityPressure >= 50f) {
            ActingEventHandler.trigger(player, ActingEvent.CLOWN8_SMILE_MASK, event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof Villager)
                || !player.isShiftKeyDown()) {
            return;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!hasSeerAbility(data, 8)) return;
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.expressionControlCooldownEndTick, now)) {
            cooldown(player, data.expressionControlCooldownEndTick, now);
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (!SpiritualityCost.tryConsume(data, EXPRESSION_CONTROL_COST)) {
            insufficient(player, EXPRESSION_CONTROL_COST);
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        data.expressionControlCooldownEndTick =
                AbilityCooldowns.start(now, EXPRESSION_CONTROL_COOLDOWN);
        player.addEffect(new MobEffectInstance(
                MobEffects.HERO_OF_THE_VILLAGE, 200, 0, false, false));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.seer.expression_control"));
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || mob.level().isClientSide()
                || !mob.getPersistentData().contains(ILLUSION_END_TAG)) {
            return;
        }
        long now = mob.level().getGameTime();
        long end = mob.getPersistentData().getLong(ILLUSION_END_TAG);
        if (now >= end) {
            mob.getPersistentData().remove(ILLUSION_END_TAG);
            mob.getPersistentData().remove(ILLUSION_X_TAG);
            mob.getPersistentData().remove(ILLUSION_Y_TAG);
            mob.getPersistentData().remove(ILLUSION_Z_TAG);
            return;
        }
        if (mob.tickCount % 20 == 0) {
            mob.setTarget(null);
            mob.getNavigation().moveTo(
                    mob.getPersistentData().getDouble(ILLUSION_X_TAG),
                    mob.getPersistentData().getDouble(ILLUSION_Y_TAG),
                    mob.getPersistentData().getDouble(ILLUSION_Z_TAG),
                    1.15d);
        }
    }

    public static boolean hasSeerAbility(PlayerMysteryData data, int requiredSequence) {
        return SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                && SeerAbilityLogic.canUseSequence(data.sequence, requiredSequence);
    }

    private static Vec3 findSafeLeapDestination(ServerPlayer player) {
        Vec3 direction = player.getLookAngle().normalize();
        for (double distance = SeerAbilityLogic.MAX_FLAME_LEAP_DISTANCE;
             distance >= 2d; distance -= 1d) {
            Vec3 delta = direction.scale(distance);
            AABB moved = player.getBoundingBox().move(delta);
            Vec3 destination = player.position().add(delta);
            if (player.level().noCollision(player, moved)
                    && !player.level().getBlockState(
                    BlockPos.containing(destination).below()).isAir()) {
                return destination;
            }
        }
        return null;
    }

    private static boolean canHarm(ServerPlayer player, LivingEntity target) {
        return !(target instanceof ServerPlayer targetPlayer)
                || player.canHarmPlayer(targetPlayer);
    }

    private static boolean hasItem(ServerPlayer player,
                                   net.minecraft.world.item.Item item, int count) {
        int found = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) found += stack.getCount();
            if (found >= count) return true;
        }
        return player.getAbilities().instabuild;
    }

    private static boolean consumeOptional(ServerPlayer player,
                                           net.minecraft.world.item.Item item, int count) {
        if (!hasItem(player, item, count)) return false;
        consumeItem(player, item, count);
        return true;
    }

    private static void consumeItem(ServerPlayer player,
                                    net.minecraft.world.item.Item item, int count) {
        if (player.getAbilities().instabuild) return;
        int remaining = count;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(item)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (remaining <= 0) return;
        }
    }

    private static boolean unavailable(ServerPlayer player, int sequence) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.seer.unavailable", sequence));
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

    private static boolean pvpBlocked(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.seer.pvp_blocked"));
        return false;
    }
}
