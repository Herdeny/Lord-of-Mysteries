package top.aurora.lordofmysteries.nightwatch;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStoppedEvent;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.OccultActivityService;
import top.aurora.lordofmysteries.registry.ModParticles;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID)
public final class NightwatchService {
    public static final ResourceLocation PATHWAY = id("sleepless");
    private static final UUID SPEED = UUID.fromString("92b8103a-61b7-42df-a71a-a38234df7c91");
    private static final UUID ATTACK = UUID.fromString("498cec2c-2f32-4300-92af-07021fdc4fe0");
    private static final Set<UUID> VISION = new HashSet<>();
    private static final Set<UUID> OWNED_VISION = new HashSet<>();

    private NightwatchService() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
    }

    public static boolean supports(ServerPlayer player) {
        var data = MysteryCapability.get(player);
        return PATHWAY.equals(data.pathway) && (data.sequence == 9 || data.sequence == 8);
    }

    public static boolean available(ServerPlayer player) {
        return supports(player) && player.isAlive() && !player.isSpectator()
                && OccultActivityService.isAvailable(player)
                && NightwatchSavedData.get(player.serverLevel()).writable();
    }

    public static int guide(ServerPlayer player) {
        boolean poet = MysteryCapability.get(player).sequence == 8;
        for (String section : poet ? new String[]{"watch", "recovery"}
                : new String[]{"intro", "brew", "abilities", "watch", "recovery"}) {
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.guide." + section));
        }
        if (poet) player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.poet.guide"));
        return status(player);
    }

    public static int status(ServerPlayer player) {
        var saved = NightwatchSavedData.get(player.serverLevel());
        var progress = saved.find(player.getUUID());
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.status",
                progress == null ? 0 : progress.stacks, progress == null ? 0 : progress.watchedTicks() / 20,
                progress == null ? 0 : progress.attackers(), VISION.contains(player.getUUID())));
        return 1;
    }

    public static boolean toggleVision(ServerPlayer player) {
        if (VISION.contains(player.getUUID())) {
            disableVision(player);
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.vision_off"));
            return true;
        }
        if (!available(player) || !Float.isFinite(MysteryCapability.get(player).spirituality)
                || MysteryCapability.get(player).spirituality < 0.5f) return false;
        VISION.add(player.getUUID());
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.vision_on"));
        pulse(player);
        return true;
    }

    public static boolean visionActive(ServerPlayer player) { return VISION.contains(player.getUUID()); }

    private static boolean ownEffect(MobEffectInstance effect) {
        return effect != null && effect.getAmplifier() == 0 && effect.getDuration() <= 240
                && !effect.isAmbient() && !effect.isVisible();
    }

    private static void disableVision(ServerPlayer player) {
        VISION.remove(player.getUUID());
        if (OWNED_VISION.remove(player.getUUID()) && ownEffect(player.getEffect(MobEffects.NIGHT_VISION))) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        var saved = NightwatchSavedData.get(player.serverLevel());
        if (!available(player)) {
            cleanup(player);
            return;
        }
        var progress = saved.getOrCreate(player.getUUID());
        long worldTime = player.serverLevel().getDayTime();
        boolean overworld = player.level().dimension().equals(Level.OVERWORLD);
        boolean night = overworld && NightwatchProgress.isNight(worldTime);
        if (progress.observe(worldTime, player.serverLevel().getGameTime(), overworld && !player.isSleeping())) {
            ActingEventHandler.trigger(player, ActingEvent.SLEEPLESS9_ALL_NIGHT, null);
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.complete"));
            pulse(player);
        }
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED, overworld ? progress.speedModifier(worldTime) : 0);
        modifier(player, Attributes.ATTACK_DAMAGE, ATTACK, night ? progress.stacks * 0.06 : 0);
        if (MysteryCapability.get(player).sequence == 8 && !VISION.contains(player.getUUID())
                && player.level().getMaxLocalRawBrightness(player.blockPosition()) < 7) {
            player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.PathfinderMob.class,
                    player.getBoundingBox().inflate(8), mob -> MidnightPoetService.validTarget(player, mob))
                    .stream().limit(8).forEach(mob -> player.serverLevel().sendParticles(player,
                            ModParticles.PAGE_GLYPH.get(), false, mob.getX(), mob.getEyeY(), mob.getZ(),
                            1, 0.1, 0.1, 0.1, 0.01));
        }
        if (VISION.contains(player.getUUID())) {
            var data = MysteryCapability.get(player);
            if (!Float.isFinite(data.spirituality) || data.spirituality < 0.5f) {
                disableVision(player);
                player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.exhausted"));
            } else {
                data.spirituality -= 0.5f;
                var effect = player.getEffect(MobEffects.NIGHT_VISION);
                if (effect == null || (OWNED_VISION.contains(player.getUUID()) && ownEffect(effect))) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, false, false, true));
                    OWNED_VISION.add(player.getUUID());
                } else {
                    OWNED_VISION.remove(player.getUUID());
                }
                for (Mob target : player.serverLevel().getEntitiesOfClass(Mob.class,
                        player.getBoundingBox().inflate(12), mob -> mob instanceof Enemy && mob.isAlive()
                                && !mob.isInvisible() && player.hasLineOfSight(mob)).stream().limit(12).toList()) {
                    player.serverLevel().sendParticles(player, ModParticles.SHADOW_FLICKER.get(), false,
                            target.getX(), target.getEyeY(), target.getZ(), 2, 0.12, 0.12, 0.12, 0.01);
                }
            }
        }
        saved.setDirty();
    }

    private static void modifier(ServerPlayer player, Attribute attribute, UUID identity, double amount) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        var current = instance.getModifier(identity);
        if (current != null && current.getAmount() == amount) return;
        instance.removeModifier(identity);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(identity,
                "Nightwatch rhythm", amount, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    public static void cleanup(ServerPlayer player) {
        disableVision(player);
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED, 0);
        modifier(player, Attributes.ATTACK_DAMAGE, ATTACK, 0);
        var progress = NightwatchSavedData.get(player.serverLevel()).find(player.getUUID());
        if (progress != null) progress.interrupt();
    }

    public static boolean canClearFatigue(ServerPlayer player) {
        if (!available(player) || !player.level().dimension().equals(Level.OVERWORLD)) return false;
        var progress = NightwatchSavedData.get(player.serverLevel()).find(player.getUUID());
        return progress != null && progress.stacks > 0 && progress.speedModifier(player.level().getDayTime()) < 0;
    }

    public static boolean clearFatigue(ServerPlayer player) {
        if (!canClearFatigue(player)) return false;
        var saved = NightwatchSavedData.get(player.serverLevel());
        saved.getOrCreate(player.getUUID()).clearedDay = Math.floorDiv(player.level().getDayTime(), 24000);
        saved.setDirty();
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED, 0);
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.refreshed"));
        pulse(player);
        return true;
    }

    public static void pulse(ServerPlayer player) {
        player.serverLevel().sendParticles(ModParticles.RITUAL_RUNE.get(),
                player.getX(), player.getY() + 1, player.getZ(), 12, 0.45, 0.6, 0.45, 0.01);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.5f, 0.7f);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player
                && player.tickCount % 20 == 0) tickPlayer(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cleanup(player);
        if (!(event.getSource().getEntity() instanceof ServerPlayer hunter) || !available(hunter)
                || !hunter.level().dimension().equals(Level.OVERWORLD)
                || !NightwatchProgress.isNight(hunter.level().getDayTime())
                || !(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy)
                || mob.getTarget() != hunter || mob.tickCount < 200) return;
        var saved = NightwatchSavedData.get(hunter.serverLevel());
        saved.getOrCreate(hunter.getUUID()).recordAttacker(mob.getUUID());
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isSleeping()
                || !available(player) || !(event.getSource().getEntity() instanceof LivingEntity)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        var saved = NightwatchSavedData.get(player.serverLevel());
        var progress = saved.getOrCreate(player.getUUID());
        long now = player.level().getGameTime();
        if (now < progress.wakeCooldown && progress.wakeCooldown - now <= 200) return;
        progress.interrupt();
        progress.wakeCooldown = now + 200;
        progress.wardUntil = now + 20;
        saved.setDirty();
        player.stopSleepInBed(true, true);
        event.setCanceled(true);
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.awaken"));
        pulse(player);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !available(player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        var progress = NightwatchSavedData.get(player.serverLevel()).find(player.getUUID());
        long now = player.level().getGameTime();
        if (progress != null && progress.wardUntil > now && progress.wardUntil - now <= 20) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var progress = NightwatchSavedData.get(player.serverLevel()).find(player.getUUID());
            if (progress != null) progress.interrupt();
        }
    }

    @SubscribeEvent
    public static void onStepSound(PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !available(player)
                || !player.level().dimension().equals(Level.OVERWORLD)
                || !NightwatchProgress.isNight(player.level().getDayTime()) || event.getSound() == null) return;
        var sound = ForgeRegistries.SOUND_EVENTS.getKey(event.getSound().value());
        if (sound != null && sound.getPath().endsWith(".step")) event.setNewVolume(event.getNewVolume() * 0.3f);
    }

    @SubscribeEvent
    public static void onPositionStepSound(PlayLevelSoundEvent.AtPosition event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
                || event.getSource() != SoundSource.PLAYERS || event.getSound() == null) return;
        var sound = ForgeRegistries.SOUND_EVENTS.getKey(event.getSound().value());
        if (sound == null || !sound.getPath().endsWith(".step")) return;
        var candidates = level.players().stream()
                .filter(player -> player.position().distanceToSqr(event.getPosition()) < 0.000001).limit(2).toList();
        if (candidates.size() == 1 && available(candidates.get(0))
                && level.dimension().equals(Level.OVERWORLD) && NightwatchProgress.isNight(level.getDayTime())) {
            event.setNewVolume(event.getNewVolume() * 0.3f);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cleanup(player);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cleanup(player);
    }

    @SubscribeEvent
    public static void onStop(ServerStoppedEvent event) {
        VISION.clear();
        OWNED_VISION.clear();
    }
}
