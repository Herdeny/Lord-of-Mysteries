package top.aurora.lordofmysteries.ability;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;

public final class MarionetteScrollItem extends Item {

    private static final String SCROLL_DATA =
            "lord_of_mysteries:marionette_scroll";
    private static final String OWNER_KEY = "owner";
    private static final String ENTITY_KEY = "entity";
    private static final String TOKEN_KEY = "token";
    private static final String ENTITY_TYPE_KEY = "entity_type";
    private static final String ENTITY_NAME_KEY = "entity_name";
    private static final int[][] DEPLOY_OFFSETS = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}
    };

    public MarionetteScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand) {
        if (!(player instanceof ServerPlayer owner)
                || !(target instanceof Mob mob)) {
            return player.level().isClientSide()
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        CaptureResult result = capture(owner, stack, mob);
        feedbackCapture(owner, result, mob);
        return result == CaptureResult.SUCCESS
                ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !isFilled(context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos requested = context.getClickedPos().relative(
                context.getClickedFace());
        DeployResult result = deploy(
                (ServerPlayer) player,
                context.getItemInHand(),
                Vec3.atBottomCenterOf(requested));
        feedbackDeploy((ServerPlayer) player, result);
        return result == DeployResult.SUCCESS
                ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isFilled(stack)) {
            if (!level.isClientSide()) {
                PlayerFeedback.send(player, Component.translatable(
                        "message.lord_of_mysteries.marionette.scroll.empty")
                        .withStyle(ChatFormatting.GRAY));
            }
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        Vec3 look = player.getLookAngle();
        Vec3 requested = player.position().add(
                look.x * 2.5d, 0d, look.z * 2.5d);
        DeployResult result = deploy(
                (ServerPlayer) player, stack, requested);
        feedbackDeploy((ServerPlayer) player, result);
        return result == DeployResult.SUCCESS
                ? InteractionResultHolder.consume(stack)
                : InteractionResultHolder.fail(stack);
    }

    public static CaptureResult capture(
            ServerPlayer owner, ItemStack scroll, Mob target) {
        if (owner == null || scroll == null || target == null
                || !(scroll.getItem() instanceof MarionetteScrollItem)
                || isFilled(scroll)) {
            return CaptureResult.INVALID_SCROLL;
        }
        PlayerMysteryData data = MysteryCapability.get(owner);
        if (!SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                || data.sequence != 5) {
            return CaptureResult.WRONG_SEQUENCE;
        }
        UUID entityId = target.getUUID();
        if (!target.isAlive()
                || target.isPassenger()
                || target.isVehicle()
                || !data.marionetteRoster.contains(entityId)
                || !owner.getUUID().equals(
                MarionetteService.ownerOf(target).orElse(null))) {
            return CaptureResult.NOT_OWNED;
        }
        if (data.marionetteStorageRecords.containsKey(entityId)) {
            return CaptureResult.ALREADY_STORED;
        }

        CompoundTag payload = new CompoundTag();
        target.saveWithoutId(payload);
        payload.putString("id", BuiltInRegistries.ENTITY_TYPE
                .getKey(target.getType()).toString());
        payload.putUUID("UUID", entityId);
        payload.remove("Passengers");
        payload.remove("Leash");
        if (!SpiritualityCost.tryConsume(
                data, MarionetteStoragePolicy.STORAGE_COST)) {
            return CaptureResult.INSUFFICIENT_SPIRITUALITY;
        }

        UUID token = UUID.randomUUID();
        data.marionetteStorageRecords.put(
                entityId,
                MarionetteStoragePolicy.createRecord(token, payload));
        writeVoucher(
                scroll,
                owner.getUUID(),
                entityId,
                token,
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType())
                        .toString(),
                target.getDisplayName().getString());
        target.discard();
        applyCooldown(owner, scroll);
        owner.serverLevel().playSound(
                null,
                owner.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                0.8f,
                0.7f);
        return CaptureResult.SUCCESS;
    }

    public static DeployResult deploy(
            ServerPlayer owner, ItemStack scroll, Vec3 requestedPosition) {
        Voucher voucher = readVoucher(scroll).orElse(null);
        if (owner == null || voucher == null) {
            return DeployResult.INVALID_SCROLL;
        }
        if (!owner.getUUID().equals(voucher.ownerId())) {
            return DeployResult.WRONG_OWNER;
        }
        PlayerMysteryData data = MysteryCapability.get(owner);
        if (!SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                || data.sequence != 5) {
            return DeployResult.WRONG_SEQUENCE;
        }
        if (!data.marionetteRoster.contains(voucher.entityId())) {
            return DeployResult.RELEASED;
        }
        CompoundTag record = data.marionetteStorageRecords.get(
                voucher.entityId());
        if (!MarionetteStoragePolicy.tokenMatches(
                record, voucher.token())) {
            return DeployResult.INVALID_TOKEN;
        }
        if (MarionetteService.findLoaded(
                owner.getServer(), voucher.entityId()).isPresent()) {
            data.marionetteStorageRecords.remove(voucher.entityId());
            return DeployResult.ALREADY_LOADED;
        }

        CompoundTag payload = MarionetteStoragePolicy.payload(record);
        Entity loaded = EntityType.loadEntityRecursive(
                payload,
                owner.serverLevel(),
                entity -> entity);
        if (!(loaded instanceof Mob mob)
                || mob.getType().getCategory() != MobCategory.MONSTER
                || mob.getMaxHealth() > MarionettePolicy.MAX_TARGET_HEALTH) {
            return DeployResult.INVALID_PAYLOAD;
        }
        mob.setUUID(voucher.entityId());
        mob.getPersistentData().putUUID(
                MarionetteService.OWNER_TAG, owner.getUUID());
        mob.setPersistenceRequired();
        mob.setTarget(null);
        mob.getNavigation().stop();
        Vec3 destination = findDeploymentDestination(
                owner.serverLevel(),
                mob,
                requestedPosition == null
                        ? owner.position() : requestedPosition);
        if (destination == null) {
            mob.discard();
            return DeployResult.NO_SAFE_POSITION;
        }
        mob.moveTo(
                destination.x,
                destination.y,
                destination.z,
                owner.getYRot(),
                0f);
        if (!owner.serverLevel().addFreshEntity(mob)) {
            return DeployResult.SPAWN_REJECTED;
        }

        data.marionetteStorageRecords.remove(voucher.entityId());
        clearVoucher(scroll);
        applyCooldown(owner, scroll);
        owner.serverLevel().playSound(
                null,
                mob.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS,
                0.8f,
                1.15f);
        return DeployResult.SUCCESS;
    }

    public static boolean isFilled(ItemStack stack) {
        return readVoucher(stack).isPresent();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isFilled(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        Optional<Voucher> voucher = readVoucher(stack);
        if (voucher.isEmpty()) {
            tooltip.add(Component.translatable(
                    "tooltip.lord_of_mysteries.marionette_scroll.empty")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable(
                    "tooltip.lord_of_mysteries.marionette_scroll.filled",
                    voucher.get().entityName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable(
                    "tooltip.lord_of_mysteries.marionette_scroll.owner_bound")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.marionette_scroll.controls",
                Math.round(MarionetteStoragePolicy.STORAGE_COST))
                .withStyle(ChatFormatting.AQUA));
    }

    private static Vec3 findDeploymentDestination(
            ServerLevel level, Mob mob, Vec3 requestedPosition) {
        BlockPos base = BlockPos.containing(requestedPosition);
        for (int verticalOffset : new int[]{0, 1, -1, 2, -2}) {
            for (int[] offset : DEPLOY_OFFSETS) {
                BlockPos feet = base.offset(
                        offset[0], verticalOffset, offset[1]);
                BlockPos floor = feet.below();
                if (!level.getWorldBorder().isWithinBounds(feet)
                        || !level.getBlockState(floor).isFaceSturdy(
                        level, floor, Direction.UP)) {
                    continue;
                }
                Vec3 destination = Vec3.atBottomCenterOf(feet);
                AABB bounds = mob.getDimensions(mob.getPose())
                        .makeBoundingBox(destination);
                if (level.noCollision(mob, bounds)) {
                    return destination;
                }
            }
        }
        return null;
    }

    private static void writeVoucher(
            ItemStack scroll,
            UUID ownerId,
            UUID entityId,
            UUID token,
            String entityType,
            String entityName) {
        CompoundTag voucher = new CompoundTag();
        voucher.putUUID(OWNER_KEY, ownerId);
        voucher.putUUID(ENTITY_KEY, entityId);
        voucher.putUUID(TOKEN_KEY, token);
        voucher.putString(ENTITY_TYPE_KEY, entityType);
        voucher.putString(ENTITY_NAME_KEY, entityName);
        scroll.getOrCreateTag().put(SCROLL_DATA, voucher);
    }

    private static Optional<Voucher> readVoucher(ItemStack scroll) {
        if (scroll == null
                || !(scroll.getItem() instanceof MarionetteScrollItem)
                || !scroll.hasTag()) {
            return Optional.empty();
        }
        CompoundTag root = scroll.getTag();
        if (root == null || !root.contains(
                SCROLL_DATA, CompoundTag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag voucher = root.getCompound(SCROLL_DATA);
        if (!voucher.hasUUID(OWNER_KEY)
                || !voucher.hasUUID(ENTITY_KEY)
                || !voucher.hasUUID(TOKEN_KEY)) {
            return Optional.empty();
        }
        return Optional.of(new Voucher(
                voucher.getUUID(OWNER_KEY),
                voucher.getUUID(ENTITY_KEY),
                voucher.getUUID(TOKEN_KEY),
                voucher.getString(ENTITY_TYPE_KEY),
                voucher.getString(ENTITY_NAME_KEY)));
    }

    private static void clearVoucher(ItemStack scroll) {
        if (scroll.hasTag() && scroll.getTag() != null) {
            scroll.getTag().remove(SCROLL_DATA);
            if (scroll.getTag().isEmpty()) scroll.setTag(null);
        }
    }

    private static void applyCooldown(
            ServerPlayer owner, ItemStack scroll) {
        if (owner.connection != null) {
            owner.getCooldowns().addCooldown(
                    scroll.getItem(),
                    MarionetteStoragePolicy.ITEM_COOLDOWN_TICKS);
        }
    }

    private static void feedbackCapture(
            ServerPlayer player, CaptureResult result, Mob target) {
        String key = switch (result) {
            case SUCCESS -> "captured";
            case INVALID_SCROLL -> "invalid_scroll";
            case WRONG_SEQUENCE -> "wrong_sequence";
            case NOT_OWNED -> "not_owned";
            case ALREADY_STORED -> "already_stored";
            case INSUFFICIENT_SPIRITUALITY -> "insufficient";
        };
        Object[] args = result == CaptureResult.SUCCESS
                ? new Object[]{target.getDisplayName(),
                Math.round(MarionetteStoragePolicy.STORAGE_COST)}
                : new Object[0];
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.marionette.scroll." + key,
                args).withStyle(result == CaptureResult.SUCCESS
                ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED));
    }

    private static void feedbackDeploy(
            ServerPlayer player, DeployResult result) {
        String key = switch (result) {
            case SUCCESS -> "deployed";
            case INVALID_SCROLL -> "invalid_scroll";
            case WRONG_OWNER -> "wrong_owner";
            case WRONG_SEQUENCE -> "wrong_sequence";
            case RELEASED -> "released";
            case INVALID_TOKEN -> "invalid_token";
            case ALREADY_LOADED -> "already_loaded";
            case INVALID_PAYLOAD -> "invalid_payload";
            case NO_SAFE_POSITION -> "no_safe_position";
            case SPAWN_REJECTED -> "spawn_rejected";
        };
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.marionette.scroll." + key)
                .withStyle(result == DeployResult.SUCCESS
                        ? ChatFormatting.AQUA : ChatFormatting.RED));
    }

    public enum CaptureResult {
        SUCCESS,
        INVALID_SCROLL,
        WRONG_SEQUENCE,
        NOT_OWNED,
        ALREADY_STORED,
        INSUFFICIENT_SPIRITUALITY
    }

    public enum DeployResult {
        SUCCESS,
        INVALID_SCROLL,
        WRONG_OWNER,
        WRONG_SEQUENCE,
        RELEASED,
        INVALID_TOKEN,
        ALREADY_LOADED,
        INVALID_PAYLOAD,
        NO_SAFE_POSITION,
        SPAWN_REJECTED
    }

    private record Voucher(
            UUID ownerId,
            UUID entityId,
            UUID token,
            String entityType,
            String entityName) {}
}
