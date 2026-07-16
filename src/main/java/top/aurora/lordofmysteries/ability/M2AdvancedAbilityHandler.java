package top.aurora.lordofmysteries.ability;

import java.util.Comparator;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.M2PathwayPotionItem;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class M2AdvancedAbilityHandler {

    public static final float SWAP_COST = 22f;
    public static final long SWAP_COOLDOWN = 1800L;
    public static final float DECOY_COST = 18f;
    public static final long DECOY_COOLDOWN = 2400L;
    public static final float RUNE_ANALYSIS_COST = 20f;
    public static final long RUNE_ANALYSIS_COOLDOWN = 600L;
    public static final float LOCKPICK_COST = 15f;
    public static final long LOCKPICK_COOLDOWN = 400L;
    public static final float TRACE_ERASURE_COST = 20f;
    public static final long TRACE_ERASURE_COOLDOWN = 1200L;
    public static final float SPATIAL_RELAY_COST = 18f;
    public static final long SPATIAL_RELAY_COOLDOWN = 300L;
    public static final float KNOWLEDGE_LINK_COST = 20f;
    public static final long KNOWLEDGE_LINK_COOLDOWN = 3600L;
    public static final float MIRROR_DOOR_COST = 25f;
    public static final long MIRROR_DOOR_COOLDOWN = 800L;
    public static final float STELLAR_DIVINATION_COST = 25f;
    public static final long STELLAR_DIVINATION_COOLDOWN = 6000L;
    public static final float STARLIGHT_WARD_COST = 30f;
    public static final long STARLIGHT_WARD_COOLDOWN = 2400L;

    private static final String HONEST_DAY = "swindler8:tracked_day";
    private static final String DIRTY_DAY = "swindler8:dirty_day";
    private static final String LAST_LOCKPICK = "cryptologist7:last_lockpick";
    private static final String RUNE_PREFIX = "cryptologist7:rune:";
    private static final String MOON_PREFIX = "astrologer7:moon:";
    private static final String MOON_DONE = "astrologer7:atlas_done";

    private M2AdvancedAbilityHandler() {}

    public static boolean use(ServerPlayer player,
                              M2FoundationAbilityHandler.AbilitySlot slot) {
        PlayerMysteryData data = MysteryCapability.get(player);
        int selected = M2AdvancedAbilityLogic.selectedSequence(
                data.sequence, player.isShiftKeyDown(), player.isSprinting());
        if (selected == 9) {
            return useFoundation(player, data, slot);
        }
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)) {
            return selected == 8
                    ? useSwindler(player, data, slot)
                    : useCryptologist(player, data, slot);
        }
        if (M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway)) {
            return selected == 8
                    ? useTrickmaster(player, data, slot)
                    : useAstrologer(player, data, slot);
        }
        return false;
    }

    private static boolean useFoundation(
            ServerPlayer player, PlayerMysteryData data,
            M2FoundationAbilityHandler.AbilitySlot slot) {
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)) {
            return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                    ? M2FoundationAbilityHandler.pilfer(player, data)
                    : M2FoundationAbilityHandler.emergencyEscape(player, data);
        }
        return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                ? M2FoundationAbilityHandler.smallSpaceTrick(player, data)
                : M2FoundationAbilityHandler.copyKnowledge(player, data);
    }

    private static boolean useSwindler(
            ServerPlayer player, PlayerMysteryData data,
            M2FoundationAbilityHandler.AbilitySlot slot) {
        return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                ? positionSwap(player, data) : decoyEscape(player, data);
    }

    private static boolean useCryptologist(
            ServerPlayer player, PlayerMysteryData data,
            M2FoundationAbilityHandler.AbilitySlot slot) {
        return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                ? runeAnalysis(player, data) : lockpickOrErase(player, data);
    }

    private static boolean useTrickmaster(
            ServerPlayer player, PlayerMysteryData data,
            M2FoundationAbilityHandler.AbilitySlot slot) {
        if (slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY) {
            return player.getMainHandItem().is(ModItems.KNOWLEDGE_COPY.get())
                    ? knowledgeLink(player, data)
                    : spatialRelay(player, data);
        }
        return mirrorDoor(player, data);
    }

    private static boolean useAstrologer(
            ServerPlayer player, PlayerMysteryData data,
            M2FoundationAbilityHandler.AbilitySlot slot) {
        return slot == M2FoundationAbilityHandler.AbilitySlot.PRIMARY
                ? stellarDivination(player, data) : starlightWard(player, data);
    }

    private static boolean positionSwap(ServerPlayer player,
                                        PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefSwapCooldownEndTick, now)) {
            return cooldown(player, data.thiefSwapCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 8d);
        if (target == null) return noTarget(player);
        boolean pvpAllowed = !(target instanceof ServerPlayer targetPlayer)
                || player.canHarmPlayer(targetPlayer);
        if (!M2AdvancedAbilityLogic.canSwap(
                player.distanceToSqr(target), player.hasLineOfSight(target),
                target instanceof ServerPlayer, pvpAllowed)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.swindler.swap_blocked"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, SWAP_COST)) {
            return insufficient(player, SWAP_COST);
        }

        boolean escaped = target instanceof Mob mob && mob.getTarget() == player;
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        target.teleportTo(playerPos.x, playerPos.y, playerPos.z);
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        data.thiefSwapCooldownEndTick = AbilityCooldowns.start(now, SWAP_COOLDOWN);
        markDirty(data);
        particles(player.serverLevel(), playerPos, ParticleTypes.REVERSE_PORTAL, 24);
        particles(player.serverLevel(), targetPos, ParticleTypes.REVERSE_PORTAL, 24);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE,
                SoundSource.PLAYERS, 0.8f, 1.2f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.swindler.swapped",
                target.getDisplayName()).withStyle(ChatFormatting.AQUA));
        if (escaped) {
            ActingEventHandler.trigger(
                    player, ActingEvent.SWINDLER8_SWAP_ESCAPE, target);
        }
        return true;
    }

    private static boolean decoyEscape(ServerPlayer player,
                                       PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefDecoyCooldownEndTick, now)) {
            return cooldown(player, data.thiefDecoyCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, DECOY_COST)) {
            return insufficient(player, DECOY_COST);
        }
        List<Mob> pursuers = player.level().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(12d),
                mob -> mob.getTarget() == player);
        pursuers.forEach(mob -> {
            mob.setTarget(null);
            mob.getNavigation().stop();
        });
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 100, 0, false, false, true));
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 100, 1, false, false, true));
        data.thiefDecoyCooldownEndTick =
                AbilityCooldowns.start(now, DECOY_COOLDOWN);
        markDirty(data);
        particles(player.serverLevel(), player.position(), ParticleTypes.SMOKE, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ARMOR_EQUIP_LEATHER,
                SoundSource.PLAYERS, 0.8f, 0.7f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.swindler.decoy", pursuers.size()));
        if (pursuers.size() >= 3) {
            ActingEventHandler.trigger(player, ActingEvent.SWINDLER8_BIG_CON, null);
        }
        return true;
    }

    private static boolean runeAnalysis(ServerPlayer player,
                                        PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefRuneCooldownEndTick, now)) {
            return cooldown(player, data.thiefRuneCooldownEndTick, now);
        }
        BlockHitResult hit = blockHit(player, 6d);
        if (hit == null) return noTarget(player);
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        if (state.isAir()) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, RUNE_ANALYSIS_COST)) {
            return insufficient(player, RUNE_ANALYSIS_COST);
        }

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String rune = blockId == null ? "unknown" : blockId.toString();
        data.actingCounters.putIfAbsent(RUNE_PREFIX + rune, 1);
        int unique = (int) data.actingCounters.keySet().stream()
                .filter(key -> key.startsWith(RUNE_PREFIX)).count();
        data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "knowledge/cryptologist/rune_analysis"));
        data.thiefRuneCooldownEndTick =
                AbilityCooldowns.start(now, RUNE_ANALYSIS_COOLDOWN);
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                hit.getBlockPos().getX() + 0.5d,
                hit.getBlockPos().getY() + 0.7d,
                hit.getBlockPos().getZ() + 0.5d,
                36, 0.4d, 0.5d, 0.4d, 0.1d);
        player.level().playSound(null, hit.getBlockPos(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.8f, 1.4f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.cryptologist.rune", unique));
        if (M2AdvancedAbilityLogic.runeMasteryReady(unique)) {
            ActingEventHandler.trigger(
                    player, ActingEvent.CRYPTOLOGIST7_ANCIENT_READ, null);
        }
        boolean shared = player.level().getEntitiesOfClass(
                ServerPlayer.class, player.getBoundingBox().inflate(6d),
                other -> other != player).size() > 0;
        if (shared) {
            ActingEventHandler.trigger(
                    player, ActingEvent.CRYPTOLOGIST7_SHARE_SECRET, null);
        }
        return true;
    }

    private static boolean lockpickOrErase(ServerPlayer player,
                                           PlayerMysteryData data) {
        BlockHitResult hit = blockHit(player, 5d);
        if (hit != null) {
            BlockState state = player.level().getBlockState(hit.getBlockPos());
            if (state.hasProperty(BlockStateProperties.OPEN)
                    && !state.getValue(BlockStateProperties.OPEN)) {
                return masterLockpick(player, data, hit, state);
            }
        }
        return eraseTraces(player, data);
    }

    private static boolean masterLockpick(ServerPlayer player,
                                          PlayerMysteryData data,
                                          BlockHitResult hit,
                                          BlockState state) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefLockpickCooldownEndTick, now)) {
            return cooldown(player, data.thiefLockpickCooldownEndTick, now);
        }
        if (!player.mayUseItemAt(hit.getBlockPos(), hit.getDirection(), ItemStack.EMPTY)) {
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, LOCKPICK_COST)) {
            return insufficient(player, LOCKPICK_COST);
        }
        player.level().setBlock(hit.getBlockPos(),
                state.setValue(BlockStateProperties.OPEN, true), 3);
        data.thiefLockpickCooldownEndTick =
                AbilityCooldowns.start(now, LOCKPICK_COOLDOWN);
        data.actingHistory.put(LAST_LOCKPICK, now);
        player.level().playSound(null, hit.getBlockPos(),
                SoundEvents.IRON_TRAPDOOR_OPEN,
                SoundSource.PLAYERS, 0.7f, 1.5f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.cryptologist.lockpick"));
        return true;
    }

    private static boolean eraseTraces(ServerPlayer player,
                                       PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefEraseCooldownEndTick, now)) {
            return cooldown(player, data.thiefEraseCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, TRACE_ERASURE_COST)) {
            return insufficient(player, TRACE_ERASURE_COST);
        }
        player.level().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(16d),
                mob -> mob.getTarget() == player).forEach(mob -> {
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                });
        player.removeEffect(MobEffects.GLOWING);
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 200, 0, false, false, true));
        data.thiefEraseCooldownEndTick =
                AbilityCooldowns.start(now, TRACE_ERASURE_COOLDOWN);
        particles(player.serverLevel(), player.position(), ParticleTypes.CLOUD, 32);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.cryptologist.erased"));
        long lastLockpick = data.actingHistory.getOrDefault(LAST_LOCKPICK, 0L);
        if (M2AdvancedAbilityLogic.perfectCrimeReady(lastLockpick, now)) {
            ActingEventHandler.trigger(
                    player, ActingEvent.CRYPTOLOGIST7_PERFECT_CRIME, null);
        }
        return true;
    }

    private static boolean spatialRelay(ServerPlayer player,
                                        PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeRelocateCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeRelocateCooldownEndTick, now);
        }
        Vec3 desired = player.position().add(player.getLookAngle().normalize().scale(4d));
        LivingEntity living = AbilityTargeting.findLookTarget(player, 6d);
        Entity target = living != null && living.getBbWidth() <= 1.5f
                && living.getBbHeight() <= 2.2f && living.getMaxHealth() <= 40f
                ? living : nearestItem(player);
        if (target == null) return noTarget(player);
        Vec3 destination = findSafeDestination(player.serverLevel(), target, desired);
        if (destination == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.trickmaster.no_space"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, SPATIAL_RELAY_COST)) {
            return insufficient(player, SPATIAL_RELAY_COST);
        }
        Vec3 origin = target.position();
        target.teleportTo(destination.x, destination.y, destination.z);
        if (target instanceof ItemEntity item) item.setPickUpDelay(0);
        data.apprenticeRelocateCooldownEndTick =
                AbilityCooldowns.start(now, SPATIAL_RELAY_COOLDOWN);
        particles(player.serverLevel(), origin, ParticleTypes.PORTAL, 24);
        particles(player.serverLevel(), destination, ParticleTypes.PORTAL, 24);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.7f, 1.6f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.trickmaster.relay",
                target.getDisplayName()));
        int audience = player.level().getEntitiesOfClass(
                ServerPlayer.class, player.getBoundingBox().inflate(10d),
                other -> other != player).size();
        if (audience >= 2) {
            ActingEventHandler.trigger(
                    player, ActingEvent.TRICKMASTER8_SHOW_OFF, null);
        }
        return true;
    }

    private static boolean knowledgeLink(ServerPlayer player,
                                         PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeLinkCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeLinkCooldownEndTick, now);
        }
        if (data.knownKnowledge.size() < 2) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.trickmaster.link_missing"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, KNOWLEDGE_LINK_COST)) {
            return insufficient(player, KNOWLEDGE_LINK_COST);
        }
        data.apprenticeLinkCooldownEndTick =
                AbilityCooldowns.start(now, KNOWLEDGE_LINK_COOLDOWN);
        boolean success = player.getRandom().nextFloat()
                < M2AdvancedAbilityLogic.knowledgeLinkChance(
                        data.knownKnowledge.size());
        if (success) {
            data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID,
                    "knowledge/apprentice/linked_formula_clue"));
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.trickmaster.link_success")
                    .withStyle(ChatFormatting.AQUA));
            ActingEventHandler.trigger(
                    player, ActingEvent.TRICKMASTER8_COMBO_DISCOVERY, null);
        } else {
            data.insanityPressure = Math.min(100f, data.insanityPressure + 5f);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.trickmaster.link_failed")
                    .withStyle(ChatFormatting.RED));
        }
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.8f, success ? 1.6f : 0.6f);
        return true;
    }

    private static boolean mirrorDoor(ServerPlayer player,
                                      PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeMirrorCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeMirrorCooldownEndTick, now);
        }
        HitResult hit = player.pick(16d, 0f, false);
        Vec3 desired = hit.getType() == HitResult.Type.MISS
                ? player.position().add(player.getLookAngle().normalize().scale(16d))
                : hit.getLocation().subtract(player.getLookAngle().normalize());
        Vec3 destination = findSafeDestination(player.serverLevel(), player, desired);
        if (destination == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.trickmaster.no_space"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, MIRROR_DOOR_COST)) {
            return insufficient(player, MIRROR_DOOR_COST);
        }
        Vec3 origin = player.position();
        int pursuers = player.level().getEntitiesOfClass(
                Mob.class, player.getBoundingBox().inflate(6d),
                mob -> mob.getTarget() == player).size();
        player.teleportTo(destination.x, destination.y, destination.z);
        data.apprenticeMirrorCooldownEndTick =
                AbilityCooldowns.start(now, MIRROR_DOOR_COOLDOWN);
        particles(player.serverLevel(), origin, ParticleTypes.REVERSE_PORTAL, 36);
        particles(player.serverLevel(), destination, ParticleTypes.REVERSE_PORTAL, 36);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE,
                SoundSource.PLAYERS, 0.9f, 1.4f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.trickmaster.mirror_door"));
        if (pursuers > 0) {
            ActingEventHandler.trigger(
                    player, ActingEvent.TRICKMASTER8_DOOR_TACTICS, null);
        }
        return true;
    }

    private static boolean stellarDivination(ServerPlayer player,
                                             PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeDivinationCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeDivinationCooldownEndTick, now);
        }
        boolean available = M2AdvancedAbilityLogic.astrologyAvailable(
                player.level().isNight(),
                player.level().canSeeSky(player.blockPosition().above()));
        if (!available) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.astrologer.sky_required"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, STELLAR_DIVINATION_COST)) {
            return insufficient(player, STELLAR_DIVINATION_COST);
        }
        long day = player.level().getDayTime() / 24000L;
        int forecast = M2AdvancedAbilityLogic.forecastIndex(
                day, player.level().getMoonPhase());
        data.apprenticeDivinationCooldownEndTick =
                AbilityCooldowns.start(now, STELLAR_DIVINATION_COOLDOWN);
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 2d, player.getZ(),
                48, 2d, 1.2d, 2d, 0.02d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 1f, 1.5f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.astrologer.forecast." + forecast)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        ActingEventHandler.trigger(
                player, ActingEvent.ASTROLOGER7_ACCURATE_FORECAST, null);
        return true;
    }

    private static boolean starlightWard(ServerPlayer player,
                                         PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeWardCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeWardCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, STARLIGHT_WARD_COST)) {
            return insufficient(player, STARLIGHT_WARD_COST);
        }
        AABB area = player.getBoundingBox().inflate(5d);
        List<ServerPlayer> protectedPlayers = player.level().getEntitiesOfClass(
                ServerPlayer.class, area, Entity::isAlive);
        protectedPlayers.forEach(target -> {
            target.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 1200, 0, false, true, true));
            target.addEffect(new MobEffectInstance(
                    MobEffects.ABSORPTION, 1200, 0, false, true, true));
            target.removeEffect(MobEffects.CONFUSION);
            target.removeEffect(MobEffects.DARKNESS);
        });
        List<Mob> spirits = player.level().getEntitiesOfClass(
                Mob.class, area,
                mob -> mob.getType().getCategory() == MobCategory.MONSTER);
        spirits.forEach(mob -> mob.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 1200, 1, false, true)));
        data.apprenticeWardCooldownEndTick =
                AbilityCooldowns.start(now, STARLIGHT_WARD_COOLDOWN);
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 0.8d, player.getZ(),
                80, 2.5d, 0.8d, 2.5d, 0.03d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 0.8f, 1.4f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.astrologer.ward",
                protectedPlayers.size()));
        if (protectedPlayers.size() > 1 && !spirits.isEmpty()) {
            ActingEventHandler.trigger(
                    player, ActingEvent.ASTROLOGER7_WARD_SAVE, null);
        }
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 200 != 0) {
            return;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)
                && data.sequence == 8) {
            tickHonestDay(player, data);
        } else if (M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway)
                && data.sequence == 7) {
            tickMoonAtlas(player, data);
        }
    }

    private static void tickHonestDay(ServerPlayer player,
                                      PlayerMysteryData data) {
        long day = player.level().getDayTime() / 24000L;
        long tracked = data.actingHistory.getOrDefault(HONEST_DAY, -1L);
        boolean dirty = data.actingCounters.getOrDefault(DIRTY_DAY, 0) > 0;
        if (M2AdvancedAbilityLogic.honestDayReady(tracked, day, dirty)) {
            ActingEventHandler.trigger(
                    player, ActingEvent.SWINDLER8_HONEST_DAY, null);
        }
        if (day > tracked) {
            data.actingHistory.put(HONEST_DAY, day);
            data.actingCounters.put(DIRTY_DAY, 0);
        }
    }

    private static void tickMoonAtlas(ServerPlayer player,
                                      PlayerMysteryData data) {
        if (data.actingCounters.getOrDefault(MOON_DONE, 0) > 0
                || !player.level().isNight()
                || !player.level().canSeeSky(player.blockPosition().above())) {
            return;
        }
        int phase = player.level().getMoonPhase();
        data.actingCounters.putIfAbsent(MOON_PREFIX + phase, 1);
        int observed = (int) data.actingCounters.keySet().stream()
                .filter(key -> key.startsWith(MOON_PREFIX)).count();
        if (M2AdvancedAbilityLogic.moonAtlasReady(observed)) {
            data.actingCounters.put(MOON_DONE, 1);
            ActingEventHandler.trigger(
                    player, ActingEvent.ASTROLOGER7_STAR_ATLAS, null);
        }
    }

    static void markDirty(PlayerMysteryData data) {
        data.actingCounters.put(DIRTY_DAY, 1);
    }

    private static ItemEntity nearestItem(ServerPlayer player) {
        return player.level().getEntitiesOfClass(
                        ItemEntity.class, player.getBoundingBox().inflate(6d),
                        item -> item.isAlive() && player.hasLineOfSight(item))
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static Vec3 findSafeDestination(ServerLevel level,
                                            Entity entity,
                                            Vec3 desired) {
        for (int yOffset = 0; yOffset <= 2; yOffset++) {
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

    private static BlockHitResult blockHit(ServerPlayer player,
                                           double range) {
        HitResult hit = player.pick(range, 0f, false);
        return hit instanceof BlockHitResult blockHit
                && hit.getType() == HitResult.Type.BLOCK ? blockHit : null;
    }

    private static void particles(ServerLevel level, Vec3 position,
                                  net.minecraft.core.particles.SimpleParticleType type,
                                  int count) {
        level.sendParticles(type, position.x, position.y + 0.7d, position.z,
                count, 0.5d, 0.7d, 0.5d, 0.04d);
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

    private static boolean cooldown(ServerPlayer player,
                                    long cooldownEnd, long now) {
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.ability.cooldown",
                Math.max(1L, AbilityCooldowns.remaining(cooldownEnd, now) / 20L)));
        return false;
    }
}
