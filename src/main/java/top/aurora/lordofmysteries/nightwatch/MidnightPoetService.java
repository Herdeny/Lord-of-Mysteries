package top.aurora.lordofmysteries.nightwatch;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.ability.AbilityTargeting;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.registry.ModParticles;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID)
public final class MidnightPoetService {
    public static final TagKey<EntityType<?>> IMMUNE = TagKey.create(Registries.ENTITY_TYPE, NightwatchService.id("poet_control_immune"));
    private static final String IMMUNITY = ProjectMystery.MOD_ID + ":poem_immunity";
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final int MAX_STATES = 256;

    private MidnightPoetService() {}

    public static boolean available(ServerPlayer player) {
        return NightwatchService.available(player) && MysteryCapability.get(player).sequence == 8;
    }

    public static boolean validTarget(ServerPlayer player, LivingEntity target) {
        return target instanceof PathfinderMob mob && mob instanceof Enemy
                && !(mob instanceof NeutralMob) && !(mob instanceof OwnableEntity)
                && !(mob instanceof AbstractPiglin)
                && !(mob instanceof top.aurora.lordofmysteries.spirit.SpiritEcologyEntity)
                && !mob.getPersistentData().contains(top.aurora.lordofmysteries.ability.MarionetteService.OWNER_TAG)
                && mob.isAlive() && !mob.isRemoved()
                && Float.isFinite(mob.getMaxHealth()) && mob.getMaxHealth() <= 80
                && !mob.getType().is(IMMUNE) && !mob.isNoAi() && !mob.isInvulnerable()
                && !mob.isPassenger() && !mob.isVehicle() && !mob.isInvisible()
                && mob.level() == player.level() && !player.isAlliedTo(mob) && !mob.isAlliedTo(player)
                && player.distanceToSqr(mob) <= 64 && player.hasLineOfSight(mob);
    }

    private static boolean check(ServerPlayer player, boolean poem) {
        if (!available(player)) return false;
        var progress = NightwatchSavedData.get(player.serverLevel()).getOrCreate(player.getUUID());
        long end = poem ? progress.poemCooldown : progress.murmurCooldown;
        if (!PoetRules.ready(end, player.level().getGameTime())) {
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.ability.cooldown",
                    Math.max(1, (end - player.level().getGameTime()) / 20)));
            return false;
        }
        if (!PoetRules.canSpend(MysteryCapability.get(player).spirituality, poem ? 15 : 18)) {
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.poet.exhausted"));
            return false;
        }
        return true;
    }

    private static void pay(ServerPlayer player, boolean poem) {
        var saved = NightwatchSavedData.get(player.serverLevel());
        var progress = saved.getOrCreate(player.getUUID());
        long now = player.level().getGameTime();
        if (poem) progress.poemCooldown = now + PoetRules.POEM_COOLDOWN;
        else progress.murmurCooldown = now + PoetRules.MURMUR_COOLDOWN;
        saved.setDirty();
        MysteryCapability.get(player).spirituality -= poem ? 15 : 18;
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.65f, poem ? 0.7f : 1.05f);
    }

    public static boolean murmur(ServerPlayer player) {
        if (!check(player, false)) return false;
        prune();
        var targets = player.serverLevel().getEntitiesOfClass(PathfinderMob.class,
                player.getBoundingBox().inflate(8), target -> validTarget(player, target))
                .stream().sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(16).toList();
        int affected = 0;
        int routed = 0;
        long now = player.level().getGameTime();
        for (var mob : targets) {
            State state = STATES.get(mob.getUUID());
            if (state != null && (state.owner != player || state.controlUntil > now)) continue;
            if (state == null) {
                if (STATES.size() >= MAX_STATES) continue;
                state = new State(player, mob);
                STATES.put(mob.getUUID(), state);
            }
            state.fear = PoetRules.addFear(state.fear);
            state.expires = now + PoetRules.FEAR_MEMORY;
            affected++;
            particles(state, false);
            if (state.fear == 5) {
                if (startFleeing(mob, player)) {
                    state.controlUntil = now + PoetRules.ROUT_TICKS;
                    state.sleeping = false;
                    install(state);
                    routed++;
                }
            }
        }
        if (affected == 0) return noTarget(player);
        pay(player, false);
        for (int index = 0; index < 24; index++) {
            double angle = index * Math.PI / 12;
            player.serverLevel().sendParticles(ModParticles.PAGE_GLYPH.get(), player.getX() + Math.cos(angle) * 2,
                    player.getY() + 0.7, player.getZ() + Math.sin(angle) * 2, 1, 0, 0.05, 0, 0.01);
        }
        if (routed >= 3) ActingEventHandler.trigger(player, ActingEvent.POET8_FEAR_ROUT, null);
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.poet.murmur", affected, routed));
        return true;
    }

    private static boolean startFleeing(PathfinderMob mob, ServerPlayer owner) {
        Vec3 away = DefaultRandomPos.getPosAway(mob, 6, 3, owner.position());
        if (away != null && moveAway(mob, owner, away)) return true;
        Vec3 direction = mob.position().subtract(owner.position());
        double heading = Math.atan2(direction.z, direction.x);
        for (double distance : new double[]{4, 2}) {
            for (double turn : new double[]{0, Math.PI / 4, -Math.PI / 4, Math.PI / 2, -Math.PI / 2}) {
                for (int vertical : new int[]{0, -1, 1}) {
                    var candidate = mob.position().add(Math.cos(heading + turn) * distance,
                            vertical, Math.sin(heading + turn) * distance);
                    if (moveAway(mob, owner, candidate)) return true;
                }
            }
        }
        return false;
    }

    private static boolean moveAway(PathfinderMob mob, ServerPlayer owner, Vec3 candidate) {
        var position = net.minecraft.core.BlockPos.containing(candidate);
        if (!mob.level().isLoaded(position)
                || Vec3.atBottomCenterOf(position).distanceToSqr(owner.position()) <= mob.distanceToSqr(owner)
                || !mob.getNavigation().isStableDestination(position)) return false;
        var path = mob.getNavigation().createPath(position, 0);
        return path != null && path.canReach() && path.getNodeCount() > 1
                && mob.getNavigation().moveTo(path, 1.2);
    }

    public static boolean sleep(ServerPlayer player) {
        return sleepTarget(player, AbilityTargeting.findLookTarget(player, 8));
    }

    public static boolean sleepTarget(ServerPlayer player, LivingEntity target) {
        if (!check(player, true)) return false;
        prune();
        if (!validTarget(player, target)) return noTarget(player);
        var mob = (PathfinderMob) target;
        long now = player.level().getGameTime();
        State previous = STATES.get(mob.getUUID());
        if (previous != null || STATES.size() >= MAX_STATES
                || !PoetRules.ready(mob.getPersistentData().getLong(IMMUNITY), now)) return noTarget(player);
        var state = new State(player, mob);
        state.sleeping = true;
        state.controlUntil = now + PoetRules.SLEEP_TICKS;
        state.expires = state.controlUntil;
        STATES.put(mob.getUUID(), state);
        mob.getPersistentData().putLong(IMMUNITY, now + 400);
        install(state);
        mob.getNavigation().stop();
        mob.setTarget(null);
        pay(player, true);
        particles(state, true);
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.poet.sleep"));
        return true;
    }

    private static boolean noTarget(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.poet.no_target"));
        return false;
    }

    private static void install(State state) {
        state.mob.goalSelector.addGoal(-1, state.goal);
        state.mob.targetSelector.addGoal(-1, state.goal);
        state.mob.setTarget(null);
    }

    private static void remove(State state) {
        state.mob.goalSelector.removeGoal(state.goal);
        state.mob.targetSelector.removeGoal(state.goal);
        if (state.controlUntil > 0) state.mob.getNavigation().stop();
    }

    public static boolean sleeping(LivingEntity entity) {
        State state = STATES.get(entity.getUUID());
        return state != null && state.sleeping && active(state);
    }

    public static int fear(LivingEntity entity) {
        State state = STATES.get(entity.getUUID());
        return state == null || !active(state) ? 0 : state.fear;
    }

    private static boolean active(State state) {
        long now = state.mob.level().getGameTime();
        return state.expires > now && state.mob.isAlive() && !state.mob.isRemoved()
                && !state.owner.isRemoved() && available(state.owner) && validTarget(state.owner, state.mob);
    }

    private static void prune() {
        var iterator = STATES.values().iterator();
        while (iterator.hasNext()) {
            var state = iterator.next();
            if (!active(state)) {
                remove(state);
                iterator.remove();
            } else if (!state.sleeping && state.controlUntil > 0
                    && state.controlUntil <= state.mob.level().getGameTime()) {
                remove(state);
                state.controlUntil = 0;
                state.fear = 0;
            }
        }
    }

    private static void particles(State state, boolean sleep) {
        if (state.mob.isInvisible()) return;
        state.owner.serverLevel().sendParticles(sleep ? ModParticles.DREAM_BUBBLE.get() : ModParticles.SHADOW_FLICKER.get(),
                state.mob.getX(), state.mob.getEyeY() + 0.2, state.mob.getZ(), sleep ? 2 : 3,
                0.18, 0.1, 0.18, 0.01);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        prune();
        for (var state : STATES.values()) {
            if (state.controlUntil > state.mob.level().getGameTime()) {
                state.mob.setTarget(null);
                if (state.sleeping) state.mob.getNavigation().stop();
                if (state.mob.level().getGameTime() % 20 == 0) particles(state, state.sleeping);
            }
        }
    }

    @SubscribeEvent
    public static void attack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && sleeping(attacker)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void damage(LivingDamageEvent event) {
        if (event.getAmount() <= 0) return;
        State state = STATES.get(event.getEntity().getUUID());
        if (state != null && state.sleeping) {
            STATES.remove(event.getEntity().getUUID());
            remove(state);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        State state = STATES.remove(event.getEntity().getUUID());
        if (state != null) remove(state);
    }

    public static void cleanup(ServerPlayer player) {
        var iterator = STATES.values().iterator();
        while (iterator.hasNext()) {
            var state = iterator.next();
            if (state.owner.getUUID().equals(player.getUUID())) {
                remove(state);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cleanup(player);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cleanup(player);
    }

    @SubscribeEvent
    public static void stop(ServerStoppedEvent event) {
        STATES.values().forEach(MidnightPoetService::remove);
        STATES.clear();
    }

    private static final class State {
        private final ServerPlayer owner;
        private final PathfinderMob mob;
        private final Goal goal;
        private int fear;
        private long expires;
        private long controlUntil;
        private boolean sleeping;

        private State(ServerPlayer owner, PathfinderMob mob) {
            this.owner = owner;
            this.mob = mob;
            this.goal = new Goal() {
                { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET)); }
                @Override
                public boolean canUse() { return controlUntil > mob.level().getGameTime() && active(State.this); }
                @Override
                public boolean canContinueToUse() { return canUse(); }
                @Override
                public void tick() {
                    mob.setTarget(null);
                    if (sleeping) mob.getNavigation().stop();
                }
            };
        }
    }
}
