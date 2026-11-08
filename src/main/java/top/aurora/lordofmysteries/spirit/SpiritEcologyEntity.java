package top.aurora.lordofmysteries.spirit;

import java.util.UUID;

import net.minecraft.ChatFormatting;
import top.aurora.lordofmysteries.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import top.aurora.lordofmysteries.registry.ModItems;

public final class SpiritEcologyEntity extends Zombie {

    private static final String OWNER_TAG = "spirit_route_owner";
    private static final String ENCOUNTER_TAG = "spirit_encounter";
    private UUID routeOwner;
    private SpiritEcologyKind ecologyKind;

    public SpiritEcologyEntity(
            EntityType<? extends SpiritEcologyEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 0.7d));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8f));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public void bind(UUID owner, SpiritEcologyKind kind) {
        if (owner == null || kind == null || kind.entityType() != getType()) {
            throw new IllegalArgumentException("spirit ecology binding required");
        }
        routeOwner = owner;
        ecologyKind = kind;
        setCustomName(Component.translatable(kind.translationKey()));
        setCustomNameVisible(true);
        setPersistenceRequired();
    }

    public boolean belongsTo(UUID owner) {
        return owner != null && owner.equals(routeOwner);
    }

    public boolean isCurrentEncounter() {
        if (!isAlive() || isRemoved() || routeOwner == null || !(level() instanceof ServerLevel serverLevel)
                || !SpiritExpeditionService.SPIRIT_WORLD.equals(level().dimension())) return false;
        SpiritExpeditionSavedData saved = SpiritExpeditionSavedData.get(serverLevel);
        SpiritExpeditionSavedData.Expedition expedition = saved.get(routeOwner);
        return expedition != null && !expedition.failed() && ecologyKind() != null
                && expedition.pendingEncounter() == ecologyKind().encounter()
                && saved.isEncounterEntity(routeOwner, getUUID())
                && !SpiritExpeditionPolicy.timedOut(level().getGameTime(), expedition.expiresAt())
                && distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(expedition.currentPad())) <= 4096d;
    }

    public boolean isCurrentFor(ServerPlayer player) {
        return player != null && player.isAlive() && !player.isSpectator()
                && player.serverLevel() == level() && belongsTo(player.getUUID())
                && distanceToSqr(player) <= 36d && isCurrentEncounter()
                && top.aurora.lordofmysteries.player.OccultActivityService.inspect(player).activity()
                == top.aurora.lordofmysteries.player.OccultActivityPolicy.Activity.SPIRIT_EXPEDITION;
    }

    public SpiritEcologyKind ecologyKind() {
        if (ecologyKind != null) return ecologyKind;
        var id = ForgeRegistries.ENTITY_TYPES.getKey(getType());
        ecologyKind = id == null ? null
                : SpiritEcologyKind.fromId(id.getPath());
        return ecologyKind;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)
                || tickCount % 80 != 0) {
            return;
        }
        SpiritEcologyKind kind = ecologyKind();
        if (kind == null) return;
        if (routeOwner != null && !isCurrentEncounter()) {
            discard();
            return;
        }
        serverLevel.sendParticles(particle(kind),
                getX(), getY() + 0.8d, getZ(),
                8, 0.35d, 0.45d, 0.35d, 0.01d);
        if (routeOwner == null) return;
        ServerPlayer owner = serverLevel.getServer()
                .getPlayerList().getPlayer(routeOwner);
        if (!isCurrentFor(owner)) return;
        applyAmbientEffect(owner, kind);
    }

    @Override
    protected InteractionResult mobInteract(
            Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (routeOwner == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spirit.entity.interact")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !belongsTo(serverPlayer.getUUID())) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spirit.entity.not_owner")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        SpiritEncounterAction action = actionFor(
                serverPlayer.getItemInHand(hand), serverPlayer.isShiftKeyDown());
        boolean studying = serverPlayer.getItemInHand(hand).is(ModItems.INVESTIGATOR_NOTES.get());
        int result = SpiritExpeditionService.interactEncounter(
                serverPlayer, this, action.id(), studying);
        if (result > 0 && !studying) discard();
        return result > 0 ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (routeOwner == null) return super.hurt(source, amount);
        if (source.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.spirit.entity.interact")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isSunSensitive() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (routeOwner != null) tag.putUUID(OWNER_TAG, routeOwner);
        SpiritEcologyKind kind = ecologyKind();
        if (kind != null) tag.putString(ENCOUNTER_TAG, kind.id());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        routeOwner = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
        ecologyKind = null;
        ecologyKind = ecologyKind();
        if (ecologyKind != null) {
            setCustomName(Component.translatable(ecologyKind.translationKey()));
            setCustomNameVisible(true);
        }
    }

    private static SpiritEncounterAction actionFor(
            ItemStack stack, boolean crouching) {
        if (stack.is(ModItems.SPIRIT_COMPASS.get())) {
            return SpiritEncounterAction.OBSERVE;
        }
        if (stack.is(ModItems.WHITE_CANDLE.get())
                || stack.is(ModItems.SPIRIT_SALT.get())) {
            return SpiritEncounterAction.AID;
        }
        if (stack.is(Items.EMERALD) || stack.is(Items.GOLD_INGOT)) {
            return SpiritEncounterAction.BARGAIN;
        }
        return crouching
                ? SpiritEncounterAction.AVOID
                : SpiritEncounterAction.OBSERVE;
    }

    private static net.minecraft.core.particles.ParticleOptions particle(
            SpiritEcologyKind kind) {
        return switch (kind) {
            case LANTERN_MOTH_SWARM, GRAVE_LANTERN -> ModParticles.CHARACTERISTIC_GLOW.get();
            case MEMORY_LEECH, PRAYER_ECHO -> ModParticles.DREAM_BUBBLE.get();
            case SPIRIT_FERRYMAN, GASLIGHT_SPECTER -> ModParticles.SPIRIT_WISP.get();
            case COLOR_EATER, THEATRE_MASKLING -> ModParticles.MIRROR_RIPPLE.get();
            case DOOR_WISP -> ModParticles.DOOR_SPARK.get();
            case WHISPER_CROW, ARCHIVE_SPIDER -> ModParticles.PAGE_GLYPH.get();
            case COMPASS_BIRD -> ModParticles.DIVINATION_THREAD.get();
        };
    }

    private static void applyAmbientEffect(
            ServerPlayer player, SpiritEcologyKind kind) {
        MobEffectInstance effect = switch (kind) {
            case MEMORY_LEECH -> new MobEffectInstance(
                    MobEffects.CONFUSION, 60, 0, false, false, true);
            case COLOR_EATER, GASLIGHT_SPECTER -> new MobEffectInstance(
                    MobEffects.DARKNESS, 60, 0, false, false, true);
            case DOOR_WISP, THEATRE_MASKLING -> new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 60, 0,
                    false, false, true);
            case PRAYER_ECHO, COMPASS_BIRD, GRAVE_LANTERN ->
                    new MobEffectInstance(
                            MobEffects.GLOWING, 60, 0,
                            false, false, true);
            default -> null;
        };
        if (effect != null) player.addEffect(effect);
    }
}
