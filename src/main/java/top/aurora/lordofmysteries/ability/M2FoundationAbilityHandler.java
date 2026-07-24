package top.aurora.lordofmysteries.ability;

import java.util.Comparator;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.acting.ActingEvent;
import top.aurora.lordofmysteries.acting.ActingEventHandler;
import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.knowledge.KnowledgeCopyItem;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.HunterPotionItem;
import top.aurora.lordofmysteries.potion.M2PathwayPotionItem;
import top.aurora.lordofmysteries.potion.SpectatorPotionItem;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class M2FoundationAbilityHandler {

    public enum AbilitySlot {
        PRIMARY,
        SECONDARY
    }

    public static final float PILFER_COST = 12f;
    public static final long PILFER_COOLDOWN = 160L;
    public static final long TARGET_PILFER_LOCK = 6000L;
    public static final float SHADOW_STEP_DRAIN = 0.8f;
    public static final float ESCAPE_COST = 15f;
    public static final long ESCAPE_COOLDOWN = 600L;
    public static final float SPACE_TRICK_COST = 8f;
    public static final long SPACE_TRICK_COOLDOWN = 200L;
    public static final float KNOWLEDGE_COPY_COST = 10f;
    public static final long KNOWLEDGE_COPY_COOLDOWN = 300L;

    private static final String PILFER_LOCK_TAG =
            ProjectMystery.MOD_ID + ":pilfer_lock_end";
    private static final String SHADOW_TICKS = "thief9:shadow_ticks";
    private static final String FIELD_NOTE_DONE = "apprentice9:field_note_done";
    private static final String BIOME_PREFIX = "apprentice9:biome:";

    private M2FoundationAbilityHandler() {}

    public static boolean use(ServerPlayer player, AbilitySlot slot) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.pathway != null
                && M3LaunchAbilityLogic.supports(
                        data.pathway.getPath(), data.sequence)) {
            return M3LaunchAbilityHandler.use(player, slot);
        }
        if (data.sequence == 7
                && (SpectatorPotionItem.SPECTATOR_PATHWAY.equals(data.pathway)
                    || HunterPotionItem.HUNTER_PATHWAY.equals(data.pathway))) {
            return M2Sequence7AbilityHandler.use(player, slot);
        }
        if ((M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)
                || M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway))
                && data.sequence <= 8 && data.sequence >= 7) {
            return M2AdvancedAbilityHandler.use(player, slot);
        }
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)
                && data.sequence == 9) {
            return slot == AbilitySlot.PRIMARY
                    ? pilfer(player, data) : emergencyEscape(player, data);
        }
        if (M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway)
                && data.sequence == 9) {
            return slot == AbilitySlot.PRIMARY
                    ? smallSpaceTrick(player, data) : copyKnowledge(player, data);
        }
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.m2_foundation.unavailable"));
        return false;
    }

    static boolean pilfer(ServerPlayer player, PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefPilferCooldownEndTick, now)) {
            return cooldown(player, data.thiefPilferCooldownEndTick, now);
        }
        LivingEntity target = AbilityTargeting.findLookTarget(player, 3d);
        if (target == null) return noTarget(player);
        if (!M2FoundationAbilityLogic.canPilfer(
                player.distanceToSqr(target),
                target.getPersistentData().getLong(PILFER_LOCK_TAG),
                now)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.thief.target_guarded"));
            return false;
        }

        ItemStack stolen;
        if (target instanceof ServerPlayer targetPlayer) {
            if (!ServerConfig.PVP_THEFT_ENABLED.get()
                    || !player.canHarmPlayer(targetPlayer)) {
                player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.thief.pvp_disabled"));
                return false;
            }
            stolen = stealFromPlayer(targetPlayer);
        } else {
            stolen = sampleLoot(player, target);
        }
        if (stolen.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.thief.nothing_to_steal"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, PILFER_COST)) {
            return insufficient(player, PILFER_COST);
        }

        data.thiefPilferCooldownEndTick = AbilityCooldowns.start(now, PILFER_COOLDOWN);
        if (data.sequence == 8) M2AdvancedAbilityHandler.markDirty(data);
        target.getPersistentData().putLong(PILFER_LOCK_TAG, now + TARGET_PILFER_LOCK);
        if (!player.getInventory().add(stolen)) player.drop(stolen, false);

        boolean alreadyAggressive = target instanceof Mob mob && mob.getTarget() != null;
        float alertChance = M2FoundationAbilityLogic.alertChance(
                target instanceof Player, alreadyAggressive, player.getLuck());
        boolean detected = player.getRandom().nextFloat() < alertChance;
        if (detected && target instanceof Mob mob) mob.setTarget(player);

        player.serverLevel().sendParticles(
                detected ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.SMOKE,
                target.getX(), target.getY() + target.getBbHeight() * 0.7d, target.getZ(),
                8, 0.25d, 0.35d, 0.25d, 0.01d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.5f, 1.5f);
        player.sendSystemMessage(Component.translatable(
                detected
                        ? "message.lord_of_mysteries.thief.pilfer_detected"
                        : "message.lord_of_mysteries.thief.pilfer_success",
                stolen.getHoverName()).withStyle(
                        detected ? ChatFormatting.RED : ChatFormatting.AQUA));

        int steals = data.actingCounters.merge("thief9:successful_steals", 1, Integer::sum);
        if (steals == 1) {
            ActingEventHandler.trigger(player, ActingEvent.THIEF9_FIRST_STEAL, target);
        }
        if (isValuable(stolen)) {
            ActingEventHandler.trigger(player, ActingEvent.THIEF9_PICK_TARGET, target);
        }
        return true;
    }

    private static ItemStack stealFromPlayer(ServerPlayer target) {
        int selected = target.getInventory().selected;
        int offset = target.getRandom().nextInt(9);
        for (int step = 0; step < 9; step++) {
            int slot = (offset + step) % 9;
            if (slot == selected) continue;
            ItemStack stack = target.getInventory().getItem(slot);
            if (stack.isEmpty()) continue;
            ItemStack stolen = stack.copy();
            stolen.setCount(1);
            stack.shrink(1);
            return stolen;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack sampleLoot(ServerPlayer player, LivingEntity target) {
        ServerLevel level = player.serverLevel();
        DamageSource source = level.damageSources().playerAttack(player);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.ENTITY);
        LootTable table = level.getServer().getLootData().getLootTable(target.getLootTable());
        List<ItemStack> loot = table.getRandomItems(params).stream()
                .filter(stack -> !stack.isEmpty())
                .toList();
        if (loot.isEmpty()) return ItemStack.EMPTY;
        ItemStack stolen = loot.get(player.getRandom().nextInt(loot.size())).copy();
        stolen.setCount(1);
        return stolen;
    }

    static boolean emergencyEscape(ServerPlayer player,
                                   PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.thiefEscapeCooldownEndTick, now)) {
            return cooldown(player, data.thiefEscapeCooldownEndTick, now);
        }
        if (!SpiritualityCost.tryConsume(data, ESCAPE_COST)) {
            return insufficient(player, ESCAPE_COST);
        }

        data.thiefEscapeCooldownEndTick = AbilityCooldowns.start(now, ESCAPE_COOLDOWN);
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(-look.x, 0d, -look.z);
        if (horizontal.lengthSqr() < 0.001d) horizontal = new Vec3(0d, 0d, 1d);
        horizontal = horizontal.normalize().scale(1.25d);
        player.setDeltaMovement(horizontal.x, 0.35d, horizontal.z);
        player.hurtMarked = true;
        player.invulnerableTime = Math.max(player.invulnerableTime, 10);
        player.serverLevel().sendParticles(ParticleTypes.SMOKE,
                player.getX(), player.getY() + 0.5d, player.getZ(),
                16, 0.4d, 0.2d, 0.4d, 0.02d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.4f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.thief.escape"));
        return true;
    }

    static boolean smallSpaceTrick(ServerPlayer player,
                                   PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeTrickCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeTrickCooldownEndTick, now);
        }
        AABB area = player.getBoundingBox().inflate(6d);
        ItemEntity target = player.level().getEntitiesOfClass(
                        ItemEntity.class, area,
                        item -> item.isAlive() && player.hasLineOfSight(item))
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (target == null) return noTarget(player);
        if (!SpiritualityCost.tryConsume(data, SPACE_TRICK_COST)) {
            return insufficient(player, SPACE_TRICK_COST);
        }

        data.apprenticeTrickCooldownEndTick =
                AbilityCooldowns.start(now, SPACE_TRICK_COOLDOWN);
        target.teleportTo(player.getX(), player.getY() + 0.4d, player.getZ());
        target.setPickUpDelay(0);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL,
                target.getX(), target.getY() + 0.2d, target.getZ(),
                18, 0.3d, 0.3d, 0.3d, 0.05d);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.4f, 1.8f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.apprentice.space_trick",
                target.getItem().getHoverName()));
        return true;
    }

    static boolean copyKnowledge(ServerPlayer player,
                                 PlayerMysteryData data) {
        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.apprenticeCopyCooldownEndTick, now)) {
            return cooldown(player, data.apprenticeCopyCooldownEndTick, now);
        }
        ResourceLocation knowledge = data.knownKnowledge.stream()
                .filter(id -> M2FoundationAbilityLogic.copyableKnowledge(id.toString()))
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .findFirst()
                .orElse(null);
        if (knowledge == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.apprentice.no_copyable_knowledge"));
            return false;
        }
        int manuscriptSlot = findItem(player, ModItems.BLANK_MANUSCRIPT.get());
        int inkSlot = findItem(player, ModItems.MYSTIC_INK.get());
        if (manuscriptSlot < 0 || inkSlot < 0) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.apprentice.copy_materials"));
            return false;
        }
        if (!SpiritualityCost.tryConsume(data, KNOWLEDGE_COPY_COST)) {
            return insufficient(player, KNOWLEDGE_COPY_COST);
        }

        data.apprenticeCopyCooldownEndTick =
                AbilityCooldowns.start(now, KNOWLEDGE_COPY_COOLDOWN);
        if (!player.getAbilities().instabuild) {
            player.getInventory().removeItem(manuscriptSlot, 1);
            player.getInventory().removeItem(inkSlot, 1);
        }
        ItemStack copy = KnowledgeCopyItem.create(
                ModItems.KNOWLEDGE_COPY.get(), knowledge, player.getUUID());
        if (!player.getInventory().add(copy)) player.drop(copy, false);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.7f, 1.3f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.apprentice.copy_created",
                Component.translatable(
                        top.aurora.lordofmysteries.knowledge.KnowledgeText
                                .translationKey(knowledge.toString())))
                .withStyle(ChatFormatting.AQUA));
        return true;
    }

    private static int findItem(ServerPlayer player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) return slot;
        }
        return -1;
    }

    private static boolean isValuable(ItemStack stack) {
        return stack.getRarity() != net.minecraft.world.item.Rarity.COMMON
                || stack.is(Items.DIAMOND)
                || stack.is(Items.EMERALD)
                || stack.is(Items.GOLD_INGOT);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        PlayerMysteryData data = MysteryCapability.get(player);
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)
                && data.sequence <= 9 && data.sequence >= 7) {
            tickShadowStep(player, data);
        } else if (player.tickCount % 200 == 0
                && M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway)
                && data.sequence <= 9 && data.sequence >= 7) {
            recordBiome(player, data);
        }
    }

    private static void tickShadowStep(ServerPlayer player,
                                       PlayerMysteryData data) {
        if (!player.isShiftKeyDown() || player.tickCount % 20 != 0) {
            if (!player.isShiftKeyDown()) data.actingCounters.put(SHADOW_TICKS, 0);
            return;
        }
        if (!SpiritualityCost.tryConsume(data, SHADOW_STEP_DRAIN)) {
            data.actingCounters.put(SHADOW_TICKS, 0);
            return;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 30, 1, false, false, true));
        int ticks = data.actingCounters.merge(SHADOW_TICKS, 20, Integer::sum);
        if (M2FoundationAbilityLogic.shadowActingReady(ticks)) {
            data.actingCounters.put(SHADOW_TICKS, 0);
            ActingEventHandler.trigger(player, ActingEvent.THIEF9_GHOST, null);
        }
    }

    private static void recordBiome(ServerPlayer player,
                                    PlayerMysteryData data) {
        if (data.actingCounters.getOrDefault(FIELD_NOTE_DONE, 0) > 0) return;
        player.level().getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> {
            data.actingCounters.putIfAbsent(BIOME_PREFIX + key.location(), 1);
            int count = (int) data.actingCounters.keySet().stream()
                    .filter(value -> value.startsWith(BIOME_PREFIX))
                    .count();
            if (M2FoundationAbilityLogic.fieldNoteReady(count)) {
                data.actingCounters.put(FIELD_NOTE_DONE, 1);
                ActingEventHandler.trigger(
                        player, ActingEvent.APPRENTICE9_FIELD_NOTE, null);
            }
        });
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)) {
            data.actingCounters.put(SHADOW_TICKS, 0);
        }
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
}
