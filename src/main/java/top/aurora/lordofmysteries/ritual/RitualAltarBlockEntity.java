package top.aurora.lordofmysteries.ritual;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import top.aurora.lordofmysteries.registry.ModBlockEntities;
import top.aurora.lordofmysteries.registry.ModItems;

public final class RitualAltarBlockEntity extends BlockEntity {

    public static final int INVOCATION_TICKS = 160;
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final RitualStateMachine machine = new RitualStateMachine();
    private int invocationTicks;

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

    public boolean start(ServerLevel level) {
        if (machine.state() == RitualStateMachine.State.COMPLETE
                || machine.state() == RitualStateMachine.State.FAILED
                || machine.state() == RitualStateMachine.State.CANCELLED) {
            machine.reset();
        }
        if (!machine.assemble(true)) return false;
        if (!machine.prime(environmentValid(level), materialsValid())) {
            machine.reset();
            return false;
        }
        invocationTicks = 0;
        boolean started = machine.invoke();
        setChanged();
        return started;
    }

    public ItemStack takeArtifact() {
        if (machine.state() != RitualStateMachine.State.COMPLETE) return ItemStack.EMPTY;
        ItemStack artifact = items.get(0);
        items.set(0, ItemStack.EMPTY);
        machine.reset();
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
        if (altar.machine.state() != RitualStateMachine.State.INVOKING) return;
        altar.invocationTicks++;
        if (altar.invocationTicks < INVOCATION_TICKS) {
            altar.setChanged();
            return;
        }

        altar.machine.beginResolve();
        boolean success = level instanceof ServerLevel serverLevel
                && altar.environmentValid(serverLevel) && altar.materialsValid();
        if (success) {
            altar.items.get(0).getOrCreateTag().putBoolean("sealed", true);
            altar.items.get(1).shrink(3);
            altar.items.get(2).shrink(5);
            altar.machine.finish(true);
        } else {
            altar.machine.finish(false);
        }
        altar.setChanged();
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
    }
}
