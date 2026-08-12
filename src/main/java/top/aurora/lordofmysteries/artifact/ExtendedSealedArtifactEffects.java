package top.aurora.lordofmysteries.artifact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

final class ExtendedSealedArtifactEffects {

    static final String WHITE_NOISE_UNTIL = "ArtifactWhiteNoiseUntil";
    private static final String REWIND_DIMENSION = "ArtifactRewindDimension";
    private static final String REWIND_X = "ArtifactRewindX";
    private static final String REWIND_Y = "ArtifactRewindY";
    private static final String REWIND_Z = "ArtifactRewindZ";
    private static final String REWIND_HEALTH = "ArtifactRewindHealth";
    private static final String CURRENT_DIMENSION = "ArtifactCurrentDimension";
    private static final String CURRENT_X = "ArtifactCurrentX";
    private static final String CURRENT_Y = "ArtifactCurrentY";
    private static final String CURRENT_Z = "ArtifactCurrentZ";
    private static final String CURRENT_HEALTH = "ArtifactCurrentHealth";
    private static final String CURRENT_TIME = "ArtifactCurrentTime";
    private static final long REWIND_SNAPSHOT_INTERVAL = 100L;
    private static final String WALK_DIMENSION = "ArtifactWalkDimension";
    private static final String WALK_X = "ArtifactWalkX";
    private static final String WALK_Y = "ArtifactWalkY";
    private static final String WALK_Z = "ArtifactWalkZ";
    private static final String WALK_DISTANCE = "ArtifactWalkDistance";
    private static final String FERRY_COOLDOWN_DAY =
            "ArtifactFerrymanCooldownDay";

    private ExtendedSealedArtifactEffects() {}

    static boolean use(
            ServerPlayer player, ItemStack stack,
            ManagedArtifactKind kind) {
        return switch (kind) {
            case WEATHERED_COIN -> useWeatheredCoin(player, stack);
            case PILGRIM_CHALK -> usePilgrimChalk(player, stack);
            case HUSHGLASS_VIAL -> useHushglassVial(player, stack);
            case WAKEFUL_SNUFFBOX -> useWakefulSnuffbox(player, stack);
            case WAYFINDER_CANDLE -> useWayfinderCandle(player, stack);
            case SALVAGE_GLOVE -> useSalvageGlove(player, stack);
            case REVERSE_WATCH -> useReverseWatch(player, stack);
            case STRINGLESS_VIOLIN -> useStringlessViolin(player, stack);
            case GLUTTONOUS_FORK -> useGluttonousFork(player, stack);
            case DEATH_LEDGER -> useDeathLedger(player, stack);
            case HUSH_BLADE -> false;
            case THOUSAND_FACE_MIRROR ->
                    useThousandFaceMirror(player, stack);
            case STORM_ANCHOR -> useStormAnchor(player, stack);
            case WHITE_NOISE_CANDELABRUM ->
                    useWhiteNoiseCandelabrum(player, stack);
            case PENITENT_IRON_SHOES ->
                    usePenitentIronShoes(player, stack);
            case GAMBLER_DICE_CUP -> useGamblerDiceCup(player, stack);
            case FERRYMAN_TICKET -> false;
            default -> false;
        };
    }

    static boolean interact(
            ServerPlayer player, ItemStack stack,
            LivingEntity target, ManagedArtifactKind kind) {
        if (kind != ManagedArtifactKind.HUSH_BLADE
                || target instanceof Player || !target.isAlive()) {
            return false;
        }
        boolean damaged = target.hurt(
                player.damageSources().playerAttack(player), 6f);
        if (!damaged) return false;
        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 160, 0));
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        AABB area = target.getBoundingBox().inflate(8d);
        for (Mob mob : player.serverLevel().getEntitiesOfClass(
                Mob.class, area,
                entity -> entity.getTarget() == target)) {
            mob.setTarget(null);
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 200, 0));
        SealedArtifactService.applyCost(
                player, stack.getItem(), 7f, 1f);
        player.serverLevel().playSound(
                null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 0.5f, 0.55f);
        return true;
    }

    static void observePassive(
            ServerPlayer player, ItemStack stack,
            ManagedArtifactKind kind) {
        if (kind == ManagedArtifactKind.REVERSE_WATCH) {
            updateRewindSnapshot(player, stack);
        } else if (kind == ManagedArtifactKind.PENITENT_IRON_SHOES) {
            updatePenitentDistance(player, stack);
        }
    }

    static void recordNearbyDeath(LivingEntity deceased) {
        if (!(deceased.level() instanceof ServerLevel level)) return;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(deceased) > 32d * 32d) continue;
            ItemStack ledger = findUsable(
                    player, ManagedArtifactKind.DEATH_LEDGER);
            if (ledger.isEmpty()) continue;
            int count = Math.min(10_000,
                    ledger.getOrCreateTag().getInt(
                            "ArtifactDeathRecords") + 1);
            ledger.getOrCreateTag().putInt(
                    "ArtifactDeathRecords", count);
            ResourceLocation type = EntityType.getKey(deceased.getType());
            ledger.getOrCreateTag().putString(
                    "ArtifactLastDeath", type.toString());
            ledger.getOrCreateTag().putLong(
                    "ArtifactLastDeathTime", level.getGameTime());
            if (count % 10 == 0) {
                SealedArtifactService.applyCost(
                        player, null, 8f, 0f);
                player.sendSystemMessage(Component.translatable(
                                "message.lord_of_mysteries.artifact.death_ledger_name",
                                count)
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    static boolean tryPreventDeath(ServerPlayer player) {
        ItemStack ticket = findUsable(
                player, ManagedArtifactKind.FERRYMAN_TICKET);
        if (ticket.isEmpty()) return false;
        long day = SealedArtifactService.currentDay(player);
        long cooldownDay = ticket.getOrCreateTag().getLong(
                FERRY_COOLDOWN_DAY);
        if (day < cooldownDay || !player.getRandom().nextBoolean()) {
            return false;
        }
        ticket.getOrCreateTag().putLong(FERRY_COOLDOWN_DAY, day + 7L);
        player.setHealth(Math.min(player.getMaxHealth(), 6f));
        player.clearFire();
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, 100, 4));
        SealedArtifactService.applyCost(
                player, ticket.getItem(), 15f, 5f);
        SealedArtifactService.recordUse(
                player, ticket, ManagedArtifactKind.FERRYMAN_TICKET);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.ferryman_saved",
                        7)
                .withStyle(ChatFormatting.DARK_AQUA));
        return true;
    }

    static boolean denyToss(ServerPlayer player, ItemStack stack) {
        if (!stack.is(ModItems.managedArtifact(
                ManagedArtifactKind.PENITENT_IRON_SHOES).get())
                || !SealedArtifactService.isUsableBoundStack(
                        player, stack,
                        ManagedArtifactKind.PENITENT_IRON_SHOES)) {
            return false;
        }
        if (MysteryCapability.get(player).pollution <= 0f) return false;
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.shoes_bound")
                .withStyle(ChatFormatting.RED));
        return true;
    }

    static boolean isWhiteNoiseProtected(
            LivingEntity entity, long gameTime) {
        return entity.getPersistentData().getLong(
                WHITE_NOISE_UNTIL) > gameTime;
    }

    private static boolean useWeatheredCoin(
            ServerPlayer player, ItemStack stack) {
        int debt = stack.getOrCreateTag().getInt("ArtifactFortuneDebt") + 1;
        stack.getOrCreateTag().putInt("ArtifactFortuneDebt", debt);
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 600, 1));
        if (debt % 2 == 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.UNLUCK, 400, 1));
        }
        SealedArtifactService.applyCost(player, stack.getItem(), 3f, 0f);
        return true;
    }

    private static boolean usePilgrimChalk(
            ServerPlayer player, ItemStack stack) {
        BlockPos outpost = MistCityOutpostSavedData.get(
                player.getServer().overworld()).outpost().orElse(null);
        if (outpost == null) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.no_outpost")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        double distance = Math.sqrt(outpost.distSqr(player.blockPosition()));
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.route_result",
                        outpost.getX(), outpost.getY(), outpost.getZ(),
                        Math.round(distance))
                .withStyle(ChatFormatting.AQUA));
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 400, 0));
        player.addEffect(new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN, 200, 0));
        SealedArtifactService.applyCost(player, stack.getItem(), 3f, 1f);
        return true;
    }

    private static boolean useHushglassVial(
            ServerPlayer player, ItemStack stack) {
        int released = 0;
        for (Mob mob : player.serverLevel().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(8d),
                entity -> entity.getTarget() == player)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            released++;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 400, 0));
        player.addEffect(new MobEffectInstance(
                MobEffects.DARKNESS, 200, 0));
        SealedArtifactService.applyCost(player, stack.getItem(), 4f, 1f);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.hushglass_result",
                        released)
                .withStyle(ChatFormatting.GRAY));
        return true;
    }

    private static boolean useWakefulSnuffbox(
            ServerPlayer player, ItemStack stack) {
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, 600, 0));
        player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 160, 0));
        SealedArtifactService.applyCost(player, stack.getItem(), 5f, 0f);
        return true;
    }

    private static boolean useWayfinderCandle(
            ServerPlayer player, ItemStack stack) {
        BlockPos outpost = MistCityOutpostSavedData.get(
                player.getServer().overworld()).outpost().orElse(null);
        if (outpost == null) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.no_outpost")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.route_result",
                        outpost.getX(), outpost.getY(), outpost.getZ(),
                        Math.round(Math.sqrt(
                                outpost.distSqr(player.blockPosition()))))
                .withStyle(ChatFormatting.GOLD));
        player.addEffect(new MobEffectInstance(
                MobEffects.GLOWING, 1200, 0));
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 300, 0));
        SealedArtifactService.applyCost(player, stack.getItem(), 4f, 2f);
        return true;
    }

    private static boolean useSalvageGlove(
            ServerPlayer player, ItemStack stack) {
        List<ItemEntity> drops = player.serverLevel().getEntitiesOfClass(
                ItemEntity.class, player.getBoundingBox().inflate(3d),
                ItemEntity::isAlive).stream()
                .sorted(Comparator.comparingDouble(
                        entity -> entity.distanceToSqr(player)))
                .limit(12)
                .toList();
        for (ItemEntity drop : drops) {
            drop.setPos(player.getX(), player.getY(), player.getZ());
            drop.setNoPickUpDelay();
        }
        int uses = stack.getOrCreateTag().getInt(
                "ArtifactSalvageUses") + 1;
        stack.getOrCreateTag().putInt("ArtifactSalvageUses", uses);
        if (uses % 3 == 0) {
            player.serverLevel().getEntitiesOfClass(
                            Mob.class, player.getBoundingBox().inflate(8d),
                            mob -> mob.getType().getCategory()
                                    == MobCategory.MONSTER)
                    .stream()
                    .min(Comparator.comparingDouble(
                            mob -> mob.distanceToSqr(player)))
                    .ifPresent(mob -> mob.setPos(
                            player.getX() + 1d,
                            player.getY(), player.getZ()));
        }
        SealedArtifactService.applyCost(player, stack.getItem(), 4f, 1f);
        player.causeFoodExhaustion(2f);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.salvage_result",
                        drops.size())
                .withStyle(ChatFormatting.AQUA));
        return true;
    }

    private static boolean useReverseWatch(
            ServerPlayer player, ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains(REWIND_DIMENSION)
                || !player.level().dimension().location().toString().equals(
                        tag.getString(REWIND_DIMENSION))) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.rewind_missing")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        Vec3 destination = new Vec3(
                tag.getDouble(REWIND_X),
                tag.getDouble(REWIND_Y),
                tag.getDouble(REWIND_Z));
        BlockPos destinationBlock = BlockPos.containing(destination);
        Vec3 movement = destination.subtract(player.position());
        if (!player.serverLevel().hasChunkAt(destinationBlock)
                || !player.serverLevel().getWorldBorder()
                        .isWithinBounds(destinationBlock)
                || !player.serverLevel().noCollision(
                        player, player.getBoundingBox().move(movement))) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.m3.invalid_target")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        player.teleportTo(destination.x, destination.y, destination.z);
        player.setHealth(Math.max(1f, Math.min(
                player.getMaxHealth(), tag.getFloat(REWIND_HEALTH))));
        player.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION, 1200, 0));
        SealedArtifactService.applyCost(player, stack.getItem(), 8f, 2f);
        return true;
    }

    private static boolean useStringlessViolin(
            ServerPlayer player, ItemStack stack) {
        int affected = 0;
        for (Mob mob : player.serverLevel().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(16d),
                entity -> entity.getType().getCategory()
                        == MobCategory.MONSTER)) {
            if (affected >= 24) break;
            mob.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
            affected++;
        }
        if (affected == 0) return false;
        player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 480, 0));
        player.addEffect(new MobEffectInstance(
                MobEffects.DARKNESS, 200, 0));
        player.serverLevel().playSound(
                null, player.blockPosition(),
                SoundEvents.NOTE_BLOCK_HARP.value(),
                SoundSource.PLAYERS, 1f, 0.45f);
        SealedArtifactService.applyCost(player, stack.getItem(), 7f, 1f);
        return affected > 0;
    }

    private static boolean useGluttonousFork(
            ServerPlayer player, ItemStack stack) {
        ItemStack foodStack = ItemStack.EMPTY;
        for (int slot = 0;
             slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEdible()) {
                foodStack = candidate;
                break;
            }
        }
        if (foodStack.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.artifact.fork_no_food")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        FoodProperties food = foodStack.getFoodProperties(player);
        if (food == null) return false;
        foodStack.shrink(1);
        player.getFoodData().eat(
                Math.min(20, food.getNutrition() * 2),
                Math.min(20f, food.getSaturationModifier() * 2f));
        player.causeFoodExhaustion(4f);
        player.containerMenu.broadcastChanges();
        SealedArtifactService.applyCost(player, stack.getItem(), 6f, 0f);
        return true;
    }

    private static boolean useDeathLedger(
            ServerPlayer player, ItemStack stack) {
        int records = stack.getOrCreateTag().getInt(
                "ArtifactDeathRecords");
        String last = stack.getOrCreateTag().getString(
                "ArtifactLastDeath");
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.death_ledger_result",
                        records, last.isBlank() ? "—" : last)
                .withStyle(ChatFormatting.DARK_PURPLE));
        SealedArtifactService.applyCost(player, stack.getItem(), 3f, 0f);
        return true;
    }

    private static boolean useThousandFaceMirror(
            ServerPlayer player, ItemStack stack) {
        ServerPlayer copied = player.serverLevel().players().stream()
                .filter(candidate -> candidate != player
                        && candidate.distanceToSqr(player) <= 8d * 8d)
                .min(Comparator.comparingDouble(
                        candidate -> candidate.distanceToSqr(player)))
                .orElse(player);
        stack.getOrCreateTag().putUUID(
                "ArtifactCopiedIdentity", copied.getUUID());
        stack.getOrCreateTag().putString(
                "ArtifactCopiedName", copied.getGameProfile().getName());
        player.getPersistentData().putUUID(
                "ArtifactMirroredIdentity", copied.getUUID());
        player.getPersistentData().putLong(
                "ArtifactMirroredUntil",
                player.serverLevel().getGameTime() + 36_000L);
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 36_000, 0));
        if (player.getRandom().nextInt(20) == 0) {
            Zombie echo = EntityType.ZOMBIE.create(player.serverLevel());
            if (echo != null) {
                echo.setCustomName(Component.literal(
                        copied.getGameProfile().getName()));
                echo.setCustomNameVisible(true);
                echo.moveTo(player.getX() + 2d, player.getY(),
                        player.getZ(), player.getYRot(), 0f);
                echo.setTarget(player);
                player.serverLevel().addFreshEntity(echo);
            }
        }
        SealedArtifactService.applyCost(player, stack.getItem(), 12f, 4f);
        return true;
    }

    private static boolean useStormAnchor(
            ServerPlayer player, ItemStack stack) {
        HitResult hit = player.pick(20d, 0f, false);
        Vec3 target = hit.getType() == HitResult.Type.MISS
                ? player.position().add(player.getLookAngle().scale(12d))
                : hit.getLocation();
        if (!stormTargetClear(target,
                player.serverLevel().players().stream()
                        .filter(candidate -> candidate != player)
                        .map(Player::position)
                        .toList())) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.m3.invalid_target")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        var lightning = EntityType.LIGHTNING_BOLT.create(
                player.serverLevel());
        if (lightning == null) return false;
        lightning.moveTo(target.x, target.y, target.z);
        lightning.setCause(player);
        player.serverLevel().addFreshEntity(lightning);
        player.serverLevel().setWeatherParameters(
                0, 1200, true, true);
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 600, 1));
        player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS, 600, 1));
        SealedArtifactService.applyCost(player, stack.getItem(), 12f, 5f);
        return true;
    }

    private static boolean useWhiteNoiseCandelabrum(
            ServerPlayer player, ItemStack stack) {
        long until = player.serverLevel().getGameTime() + 400L;
        int protectedEntities = 0;
        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(8d))) {
            entity.getPersistentData().putLong(WHITE_NOISE_UNTIL, until);
            protectedEntities++;
        }
        player.getPersistentData().putLong(WHITE_NOISE_UNTIL, until);
        player.addEffect(new MobEffectInstance(
                MobEffects.DARKNESS, 400, 0));
        SealedArtifactService.applyCost(player, stack.getItem(), 10f, 3f);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.white_noise_result",
                        protectedEntities)
                .withStyle(ChatFormatting.GRAY));
        return true;
    }

    private static boolean usePenitentIronShoes(
            ServerPlayer player, ItemStack stack) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.shoes_progress",
                        Math.round(stack.getOrCreateTag().getDouble(
                                WALK_DISTANCE)),
                        Math.round(data.pollution))
                .withStyle(ChatFormatting.GRAY));
        SealedArtifactService.applyCost(player, stack.getItem(), 2f, 0f);
        return true;
    }

    private static boolean useGamblerDiceCup(
            ServerPlayer player, ItemStack stack) {
        int roll = player.getRandom().nextInt(100) + 1;
        String outcome;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (roll >= 60) {
            long reward = 12L + roll;
            data.moneyPence = saturatingAdd(data.moneyPence, reward);
            data.markDirty(PlayerDataSection.SOCIAL);
            player.addEffect(new MobEffectInstance(
                    MobEffects.LUCK, 600, 1));
            outcome = "reward";
        } else if (roll <= 10) {
            outcome = removeGambleStake(player) ? "lost_item" : "empty";
            player.addEffect(new MobEffectInstance(
                    MobEffects.UNLUCK, 1200, 1));
        } else {
            outcome = "uncertain";
            player.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, 200, 0));
        }
        SealedArtifactService.applyCost(player, stack.getItem(), 9f, 2f);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.artifact.dice_result",
                        roll,
                        Component.translatable(
                                "artifact_dice.lord_of_mysteries."
                                        + outcome))
                .withStyle(roll >= 60
                        ? ChatFormatting.GOLD : ChatFormatting.RED));
        return true;
    }

    private static void updateRewindSnapshot(
            ServerPlayer player, ItemStack stack) {
        var tag = stack.getOrCreateTag();
        long gameTime = player.serverLevel().getGameTime();
        if (!rewindSnapshotDue(
                tag.getLong(CURRENT_TIME), gameTime)) {
            return;
        }
        if (tag.contains(CURRENT_DIMENSION)) {
            tag.putString(REWIND_DIMENSION,
                    tag.getString(CURRENT_DIMENSION));
            tag.putDouble(REWIND_X, tag.getDouble(CURRENT_X));
            tag.putDouble(REWIND_Y, tag.getDouble(CURRENT_Y));
            tag.putDouble(REWIND_Z, tag.getDouble(CURRENT_Z));
            tag.putFloat(REWIND_HEALTH, tag.getFloat(CURRENT_HEALTH));
        }
        tag.putString(CURRENT_DIMENSION,
                player.level().dimension().location().toString());
        tag.putDouble(CURRENT_X, player.getX());
        tag.putDouble(CURRENT_Y, player.getY());
        tag.putDouble(CURRENT_Z, player.getZ());
        tag.putFloat(CURRENT_HEALTH, player.getHealth());
        tag.putLong(CURRENT_TIME, gameTime);
    }

    static boolean rewindSnapshotDue(
            long previousGameTime, long currentGameTime) {
        return previousGameTime <= 0L
                || currentGameTime < previousGameTime
                || currentGameTime - previousGameTime
                >= REWIND_SNAPSHOT_INTERVAL;
    }

    static boolean stormTargetClear(
            Vec3 target, List<Vec3> otherPlayers) {
        return otherPlayers.stream().noneMatch(
                position -> position.distanceToSqr(target) <= 16d);
    }

    private static void updatePenitentDistance(
            ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 140, 0,
                false, false));
        var tag = stack.getOrCreateTag();
        String dimension = player.level().dimension().location().toString();
        if (!dimension.equals(tag.getString(WALK_DIMENSION))) {
            saveWalkPosition(player, tag, dimension);
            return;
        }
        double dx = player.getX() - tag.getDouble(WALK_X);
        double dy = player.getY() - tag.getDouble(WALK_Y);
        double dz = player.getZ() - tag.getDouble(WALK_Z);
        double step = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (step <= 32d) {
            double distance = tag.getDouble(WALK_DISTANCE) + step;
            PlayerMysteryData data = MysteryCapability.get(player);
            int reductions = (int) Math.floor(distance / 1000d);
            if (reductions > 0 && data.pollution > 0f) {
                float before = data.pollution;
                data.pollution = Math.max(0f,
                        data.pollution - reductions);
                data.markDirty(PlayerDataSection.CORE);
                distance -= reductions * 1000d;
                player.sendSystemMessage(Component.translatable(
                                "message.lord_of_mysteries.artifact.shoes_cleansed",
                                Math.round(before - data.pollution),
                                Math.round(data.pollution))
                        .withStyle(ChatFormatting.AQUA));
            }
            tag.putDouble(WALK_DISTANCE, distance);
        }
        saveWalkPosition(player, tag, dimension);
    }

    private static void saveWalkPosition(
            ServerPlayer player,
            net.minecraft.nbt.CompoundTag tag,
            String dimension) {
        tag.putString(WALK_DIMENSION, dimension);
        tag.putDouble(WALK_X, player.getX());
        tag.putDouble(WALK_Y, player.getY());
        tag.putDouble(WALK_Z, player.getZ());
    }

    private static ItemStack findUsable(
            ServerPlayer player, ManagedArtifactKind kind) {
        for (int slot = 0;
             slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.managedArtifact(kind).get())
                    && SealedArtifactService.isUsableBoundStack(
                            player, stack, kind)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean removeGambleStake(ServerPlayer player) {
        List<Integer> candidates = new ArrayList<>();
        for (int slot = 0;
             slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEmpty()
                    || candidate.hasTag()
                    && candidate.getTag().hasUUID(
                            SealedArtifactService.INSTANCE_TAG)) {
                continue;
            }
            candidates.add(slot);
        }
        if (candidates.isEmpty()) return false;
        int slot = candidates.get(player.getRandom().nextInt(
                candidates.size()));
        ItemStack candidate = player.getInventory().getItem(slot);
        candidate.shrink(1);
        player.containerMenu.broadcastChanges();
        return true;
    }

    private static long saturatingAdd(long value, long increase) {
        if (increase > 0L && value > Long.MAX_VALUE - increase) {
            return Long.MAX_VALUE;
        }
        return value + increase;
    }
}
