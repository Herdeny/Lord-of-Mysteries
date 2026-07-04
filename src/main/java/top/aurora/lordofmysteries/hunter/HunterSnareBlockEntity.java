package top.aurora.lordofmysteries.hunter;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.ability.HunterAbilityHandler;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModBlockEntities;

public final class HunterSnareBlockEntity extends BlockEntity {

    @Nullable
    private UUID owner;
    private boolean triggered;

    public HunterSnareBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUNTER_SNARE.get(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public void trigger(LivingEntity target) {
        if (triggered || !(level instanceof ServerLevel serverLevel)
                || owner != null && owner.equals(target.getUUID())) {
            return;
        }
        triggered = true;
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true));
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 100, 0, false, true));
        serverLevel.sendParticles(ParticleTypes.CRIT,
                target.getX(), target.getY() + 0.2, target.getZ(),
                12, 0.25, 0.15, 0.25, 0.04);
        serverLevel.playSound(null, worldPosition,
                SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f);

        if (owner != null) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(owner);
            if (player != null) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "message.lord_of_mysteries.hunter.snare_triggered",
                        target.getDisplayName()));
                PlayerMysteryData data = MysteryCapability.get(player);
                if (data.sequence == 9
                        && HunterAbilityHandler.hasHunterAbility(data, 9)
                        && HunterAbilityHandler.validHuntTarget(target)) {
                    int traps = data.actingCounters.merge(
                            "hunter9:effective_traps", 1, Integer::sum);
                    if (traps >= 3) {
                        data.actingCounters.put("hunter9:effective_traps", 0);
                        ActingEventHandler.trigger(
                                player, ActingEvent.HUNTER9_TRAP_SETUP, target);
                    }
                }
            }
        }
        serverLevel.destroyBlock(worldPosition, false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("owner", owner);
        tag.putBoolean("triggered", triggered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        triggered = tag.getBoolean("triggered");
    }
}
