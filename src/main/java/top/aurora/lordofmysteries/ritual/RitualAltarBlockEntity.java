package top.aurora.lordofmysteries.ritual;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModBlockEntities;
import top.aurora.lordofmysteries.registry.ModEntities;
import top.aurora.lordofmysteries.registry.ModItems;

public final class RitualAltarBlockEntity extends BlockEntity {

    public static final int INVOCATION_TICKS = 160;
    private static final int STRUCTURE_RECHECK_INTERVAL = 20;
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final RitualStateMachine machine = new RitualStateMachine();
    private int invocationTicks;
    private int leaderOfflineTicks;
    private UUID leader;
    private RitualResolutionLogic.Outcome lastOutcome;

    public RitualAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RITUAL_ALTAR.get(), pos, state);
    }

    public boolean insert(ItemStack held, Player player) {
        int slot = slotFor(held);
        if (slot < 0 || machine.state() == RitualStateMachine.State.INVOKING) return false;
        int limit = requiredCount(slot);
        int current = items.get(slot).getCount();
        if (current >= limit) return false;
        int moved = Math.min(limit - current, held.getCount());
        if (items.get(slot).isEmpty()) {
            ItemStack inserted = held.copy();
            inserted.setCount(moved);
            items.set(slot, inserted);
        } else {
            items.get(slot).grow(moved);
        }
        if (!player.getAbilities().instabuild) held.shrink(moved);
        setChanged();
        return true;
    }

    public boolean start(ServerLevel level, Player player) {
        if (machine.state() == RitualStateMachine.State.COMPLETE
                || machine.state() == RitualStateMachine.State.FAILED
                || machine.state() == RitualStateMachine.State.CANCELLED) {
            machine.reset();
            lastOutcome = null;
        }
        MultiBlockRitualDetector.Inspection structure =
                MultiBlockRitualDetector.inspect(level, worldPosition);
        if (!machine.assemble(structure.complete())) return false;
        if (!machine.prime(environmentValid(level), materialsValid())) {
            machine.reset();
            return false;
        }
        invocationTicks = 0;
        leaderOfflineTicks = 0;
        leader = player.getUUID();
        boolean started = machine.invoke();
        setChanged();
        return started;
    }

    public ItemStack takeArtifact() {
        if (machine.state() != RitualStateMachine.State.COMPLETE) return ItemStack.EMPTY;
        ItemStack artifact = items.get(0);
        items.set(0, ItemStack.EMPTY);
        machine.reset();
        leader = null;
        leaderOfflineTicks = 0;
        lastOutcome = null;
        setChanged();
        return artifact;
    }

    public RitualStateMachine.State state() {
        return machine.state();
    }

    public int invocationTicks() {
        return invocationTicks;
    }

    public boolean materialsValid() {
        return items.get(0).is(ModItems.ETERNAL_MATCHBOX.get())
                && items.get(1).is(ModItems.PURE_WATER.get()) && items.get(1).getCount() >= 3
                && items.get(2).is(Items.BLUE_ORCHID) && items.get(2).getCount() >= 5
                && items.get(3).is(ModItems.WHITE_CANDLE.get()) && items.get(3).getCount() >= 8;
    }

    public boolean environmentValid(ServerLevel level) {
        int y = getBlockPos().getY();
        return !level.isDay() && !level.isRaining() && y >= -60 && y <= 100;
    }

    public List<ItemStack> itemsForDrop() {
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack stack : items) if (!stack.isEmpty()) drops.add(stack.copy());
        return drops;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  RitualAltarBlockEntity altar) {
        if (altar.machine.state() != RitualStateMachine.State.INVOKING
                || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ServerPlayer leaderPlayer = altar.leader == null ? null
                : serverLevel.getServer().getPlayerList().getPlayer(altar.leader);
        int offlineTicks = leaderPlayer == null ? altar.leaderOfflineTicks + 1 : 0;
        RitualRecoveryLogic.Action recovery = RitualRecoveryLogic.decide(
                true, leaderPlayer != null, offlineTicks);
        if (recovery == RitualRecoveryLogic.Action.PAUSE) {
            altar.leaderOfflineTicks = offlineTicks;
            altar.setChanged();
            return;
        }
        if (recovery == RitualRecoveryLogic.Action.CANCEL) {
            altar.machine.cancel();
            altar.leaderOfflineTicks = 0;
            altar.setChanged();
            return;
        }
        altar.leaderOfflineTicks = 0;
        altar.invocationTicks++;
        if (altar.invocationTicks % STRUCTURE_RECHECK_INTERVAL == 0
                && !MultiBlockRitualDetector.inspect(serverLevel, pos).complete()) {
            altar.machine.cancel();
            altar.notifyLeader(serverLevel,
                    "message.lord_of_mysteries.ritual.structure_broken");
            serverLevel.playSound(null, pos,
                    SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8f, 0.7f);
            altar.setChanged();
            return;
        }
        if (altar.invocationTicks < INVOCATION_TICKS) {
            altar.setChanged();
            return;
        }

        altar.machine.beginResolve();
        MultiBlockRitualDetector.Inspection structure =
                MultiBlockRitualDetector.inspect(serverLevel, pos);
        boolean materialsValid = altar.materialsValid();
        if (!structure.complete() || !materialsValid) {
            altar.machine.cancel();
            altar.notifyLeader(serverLevel,
                    "message.lord_of_mysteries.ritual.cancelled");
            altar.setChanged();
            return;
        }

        boolean qualifiedLeader = leaderPlayer != null
                && qualifiedLeader(MysteryCapability.get(leaderPlayer));
        float score = RitualResolutionLogic.completionScore(
                materialsValid,
                altar.environmentValid(serverLevel),
                structure.completion(),
                qualifiedLeader);
        float randomDelta = (serverLevel.random.nextFloat() - 0.5f) * 0.30f;
        altar.lastOutcome = RitualResolutionLogic.escalateFailure(
                RitualResolutionLogic.resolve(score, randomDelta),
                serverLevel.random.nextFloat());
        altar.consumeMaterials();

        if (altar.lastOutcome.success()) {
            altar.items.get(0).getOrCreateTag().putBoolean("sealed", true);
            altar.items.get(0).getOrCreateTag().putString(
                    "seal_quality",
                    altar.lastOutcome == RitualResolutionLogic.Outcome.PERFECT
                            ? "perfect" : "stable");
            altar.machine.finish(true);
            altar.completeSuccess(serverLevel);
        } else {
            altar.machine.finish(false);
            altar.applyFailure(serverLevel, leaderPlayer);
        }
        altar.setChanged();
    }

    private static boolean qualifiedLeader(PlayerMysteryData data) {
        return data.pathway != null && data.sequence >= 0 && data.sequence <= 9;
    }

    private void consumeMaterials() {
        items.get(1).shrink(3);
        items.get(2).shrink(5);
    }

    private void completeSuccess(ServerLevel level) {
        boolean perfect = lastOutcome == RitualResolutionLogic.Outcome.PERFECT;
        level.sendParticles(perfect ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1,
                worldPosition.getZ() + 0.5,
                perfect ? 36 : 20, 0.8, 0.5, 0.8, 0.03);
        level.playSound(null, worldPosition,
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS,
                0.9f, perfect ? 1.2f : 1f);
        notifyLeader(level, perfect
                ? "message.lord_of_mysteries.ritual.perfect"
                : "message.lord_of_mysteries.ritual.success");
    }

    private void applyFailure(ServerLevel level, ServerPlayer leaderPlayer) {
        if (leaderPlayer != null) {
            PlayerMysteryData data = MysteryCapability.get(leaderPlayer);
            if (lastOutcome == RitualResolutionLogic.Outcome.FAILURE) {
                data.insanityPressure = Math.min(100f, data.insanityPressure + 10f);
            } else if (lastOutcome == RitualResolutionLogic.Outcome.SEVERE_FAILURE) {
                data.pollution = Math.min(100f, data.pollution + 15f);
                data.insanityPressure = Math.min(100f, data.insanityPressure + 10f);
            } else {
                data.pollution = Math.min(100f, data.pollution + 25f);
                data.insanityPressure = Math.min(100f, data.insanityPressure + 20f);
            }
        }

        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1,
                worldPosition.getZ() + 0.5,
                28, 0.9, 0.5, 0.9, 0.05);
        if (lastOutcome == RitualResolutionLogic.Outcome.SEVERE_FAILURE
                || lastOutcome == RitualResolutionLogic.Outcome.CATASTROPHE) {
            spawnWraith(level, leaderPlayer);
        }
        if (lastOutcome == RitualResolutionLogic.Outcome.CATASTROPHE) {
            level.explode(null,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1,
                    worldPosition.getZ() + 0.5,
                    3f, Level.ExplosionInteraction.NONE);
        } else {
            level.playSound(null, worldPosition,
                    SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.9f, 0.7f);
        }

        String key = switch (lastOutcome) {
            case FAILURE -> "message.lord_of_mysteries.ritual.failure";
            case SEVERE_FAILURE -> "message.lord_of_mysteries.ritual.severe_failure";
            case CATASTROPHE -> "message.lord_of_mysteries.ritual.catastrophe";
            default -> "message.lord_of_mysteries.ritual.failure";
        };
        notifyLeader(level, key);
    }

    private void spawnWraith(ServerLevel level, ServerPlayer target) {
        var entity = ModEntities.SEER_BREAKDOWN.get().create(level);
        if (entity == null) return;
        entity.moveTo(worldPosition.getX() + 0.5,
                worldPosition.getY() + 1,
                worldPosition.getZ() + 0.5,
                level.random.nextFloat() * 360f, 0f);
        if (target != null) entity.setTarget(target);
        level.addFreshEntity(entity);
    }

    private void notifyLeader(ServerLevel level, String translationKey) {
        if (leader == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(leader);
        if (player != null) {
            player.sendSystemMessage(Component.translatable(translationKey));
        }
    }

    private static int slotFor(ItemStack stack) {
        if (stack.is(ModItems.ETERNAL_MATCHBOX.get())) return 0;
        if (stack.is(ModItems.PURE_WATER.get())) return 1;
        if (stack.is(Items.BLUE_ORCHID)) return 2;
        if (stack.is(ModItems.WHITE_CANDLE.get())) return 3;
        return -1;
    }

    private static int requiredCount(int slot) {
        return switch (slot) {
            case 0 -> 1;
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 8;
            default -> 0;
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) i);
            items.get(i).save(itemTag);
            list.add(itemTag);
        }
        tag.put("items", list);
        tag.putString("ritual_state", machine.state().name());
        tag.putInt("invocation_ticks", invocationTicks);
        tag.putInt("leader_offline_ticks", leaderOfflineTicks);
        if (leader != null) tag.putUUID("ritual_leader", leader);
        if (lastOutcome != null) tag.putString("ritual_outcome", lastOutcome.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < items.size()) items.set(slot, ItemStack.of(itemTag));
        }
        try {
            machine.restore(RitualStateMachine.State.valueOf(tag.getString("ritual_state")));
        } catch (IllegalArgumentException ignored) {
            machine.reset();
        }
        invocationTicks = tag.getInt("invocation_ticks");
        leaderOfflineTicks = Math.max(0, tag.getInt("leader_offline_ticks"));
        leader = tag.hasUUID("ritual_leader") ? tag.getUUID("ritual_leader") : null;
        try {
            lastOutcome = RitualResolutionLogic.Outcome.valueOf(
                    tag.getString("ritual_outcome"));
        } catch (IllegalArgumentException ignored) {
            lastOutcome = null;
        }
    }
}
