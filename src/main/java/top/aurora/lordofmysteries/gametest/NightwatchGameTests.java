package top.aurora.lordofmysteries.gametest;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.aurora.lordofmysteries.nightwatch.NightwatchSavedData;
import top.aurora.lordofmysteries.nightwatch.NightwatchService;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.potion.CrucibleBlockEntity;
import top.aurora.lordofmysteries.potion.PotionQuality;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.registry.ModBlocks;
import top.aurora.lordofmysteries.registry.ModEntities;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.spirit.SpiritExpeditionSavedData;
import top.aurora.lordofmysteries.spirit.SpiritProjection;

@PrefixGameTestTemplate(false)
public final class NightwatchGameTests {
    private NightwatchGameTests() {}

    private static ServerPlayer player(GameTestHelper helper, boolean sleepless) {
        var level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "nightwatch-qa")) {
            @Override
            public void sendSystemMessage(Component message) {}
            @Override
            public void displayClientMessage(Component message, boolean actionBar) {}
            @Override
            protected void onEffectAdded(MobEffectInstance effect, Entity source) {}
            @Override
            protected void onEffectUpdated(MobEffectInstance effect, boolean force, Entity source) {}
            @Override
            protected void onEffectRemoved(MobEffectInstance effect) {}
            @Override
            public void stopSleepInBed(boolean immediately, boolean updateLevel) { clearSleepingPos(); }
            @Override
            public boolean isSpectator() { return getPersistentData().getBoolean("qa_spectator"); }
        };
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 2, 3))));
        player.getAbilities().mayBuild = true;
        if (sleepless) {
            var data = MysteryCapability.get(player);
            data.pathway = NightwatchService.PATHWAY;
            data.sequence = 9;
            data.spirituality = data.spiritualityMax = 114;
        }
        return player;
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchBrewUsesActualCrucibleAndGrantsOneIdentity(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = player(helper, false);
        var position = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlock(position.below(), Blocks.CAMPFIRE.defaultBlockState(), 3);
        level.setBlock(position, ModBlocks.CRUCIBLE.get().defaultBlockState(), 3);
        var crucible = (CrucibleBlockEntity) level.getBlockEntity(position);
        for (var ingredient : new net.minecraft.world.item.Item[]{ModItems.NIGHT_OWL_OPTIC_NERVE.get(),
                ModItems.NIGHTWATCH_EXTRACT.get(), ModItems.ASH_POWDER.get()}) {
            var held = new ItemStack(ingredient);
            helper.assertTrue(crucible.insert(held, owner) && held.isEmpty(), "each real ingredient consumed once");
        }
        helper.assertTrue(crucible.startBrewing(), "recipe starts");
        for (int tick = 0; tick < 1200; tick++) CrucibleBlockEntity.serverTick(level, position, level.getBlockState(position), crucible);
        ItemStack potion = crucible.takeResult();
        helper.assertTrue(potion.is(ModItems.SLEEPLESS_POTION_9.get()), "real crucible emits Sleepless potion");
        helper.assertTrue(SeerPotionItem.getQuality(potion) != PotionQuality.CONTAMINATED, "campfire produces usable potion");
        potion.getItem().finishUsingItem(potion, level, owner);
        var data = MysteryCapability.get(owner);
        helper.assertTrue(potion.isEmpty() && NightwatchService.supports(owner) && data.spiritualityMax == 114,
                "first potion grants exact pathway, sequence and spirituality");
        helper.assertTrue(data.characteristicBundles.size() == 1
                && owner.getInventory().countItem(ModItems.ACTING_IDENTITY_CARD.get()) == 1
                && owner.getInventory().countItem(ModItems.ACTING_REFLECTION_JOURNAL.get()) == 1, "ledger and starter identity granted once");
        var duplicate = new ItemStack(ModItems.SLEEPLESS_POTION_9.get());
        duplicate.getItem().finishUsingItem(duplicate, level, owner);
        helper.assertTrue(duplicate.getCount() == 1 && data.characteristicBundles.size() == 1, "duplicate potion rejected without loss");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchActivityAndSpectatorRejectWithoutMutation(GameTestHelper helper) {
        var owner = player(helper, false);
        var level = helper.getLevel();
        var potion = new ItemStack(ModItems.SLEEPLESS_POTION_9.get());
        owner.getPersistentData().putBoolean("qa_spectator", true);
        potion.getItem().finishUsingItem(potion, level, owner);
        helper.assertTrue(MysteryCapability.get(owner).pathway == null && potion.getCount() == 1, "spectator cannot drink");
        owner.getPersistentData().putBoolean("qa_spectator", false);
        var routes = SpiritExpeditionSavedData.get(level);
        routes.start(owner.getUUID(), level.dimension().location(), owner.blockPosition(), SpiritProjection.THEATRE, 11L, level.getGameTime());
        try {
            potion.getItem().finishUsingItem(potion, level, owner);
            helper.assertTrue(MysteryCapability.get(owner).pathway == null && potion.getCount() == 1, "active expedition cannot advance");
            var data = MysteryCapability.get(owner);
            data.pathway = NightwatchService.PATHWAY;
            data.sequence = 9;
            data.spirituality = 50;
            helper.assertTrue(!NightwatchService.toggleVision(owner) && data.spirituality == 50, "activity cannot drain or toggle vision");
        } finally { routes.remove(owner.getUUID()); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchVisionRespectsForeignEffectsEnergyAndLogout(GameTestHelper helper) {
        var owner = player(helper, true);
        var data = MysteryCapability.get(owner);
        owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 1));
        helper.assertTrue(NightwatchService.toggleVision(owner), "vision can start");
        NightwatchService.tickPlayer(owner);
        helper.assertTrue(data.spirituality == 113.5f && owner.getEffect(MobEffects.NIGHT_VISION).getAmplifier() == 1,
                "exact half point cost does not overwrite a stronger foreign effect");
        NightwatchService.cleanup(owner);
        helper.assertTrue(owner.hasEffect(MobEffects.NIGHT_VISION), "cleanup preserves foreign effect");
        owner.removeAllEffects();
        data.spirituality = 0.5f;
        NightwatchService.toggleVision(owner);
        NightwatchService.tickPlayer(owner);
        helper.assertTrue(owner.hasEffect(MobEffects.NIGHT_VISION) && data.spirituality == 0, "last half point can be used");
        NightwatchService.tickPlayer(owner);
        helper.assertTrue(!NightwatchService.visionActive(owner) && !owner.hasEffect(MobEffects.NIGHT_VISION), "exhaustion removes only own effect");
        data.spirituality = 10;
        NightwatchService.toggleVision(owner);
        NightwatchService.tickPlayer(owner);
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(owner));
        helper.assertTrue(!NightwatchService.visionActive(owner) && !owner.hasEffect(MobEffects.NIGHT_VISION), "logout clears transient vision");
        data.spirituality = Float.NaN;
        helper.assertTrue(!NightwatchService.toggleVision(owner), "invalid energy fails closed");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchDayNightCoffeeAndAttributesAreReversible(GameTestHelper helper) {
        var level = helper.getLevel();
        long before = level.getDayTime();
        var owner = player(helper, true);
        var progress = NightwatchSavedData.get(level).getOrCreate(owner.getUUID());
        progress.stacks = 3;
        double base = owner.getAttributeValue(Attributes.MOVEMENT_SPEED);
        try {
            level.setDayTime(14000);
            NightwatchService.tickPlayer(owner);
            helper.assertTrue(Math.abs(owner.getAttributeValue(Attributes.MOVEMENT_SPEED) - base * 1.12) < 0.00001, "night speed buff");
            level.setDayTime(24000);
            NightwatchService.tickPlayer(owner);
            helper.assertTrue(Math.abs(owner.getAttributeValue(Attributes.MOVEMENT_SPEED) - base * 0.7) < 0.00001, "day fatigue");
            var coffee = new ItemStack(ModItems.STRONG_COFFEE.get());
            var result = coffee.getItem().finishUsingItem(coffee, level, owner);
            helper.assertTrue(result.is(Items.GLASS_BOTTLE) && progress.stacks == 3, "coffee returns bottle and preserves earned stacks");
            helper.assertTrue(Math.abs(owner.getAttributeValue(Attributes.MOVEMENT_SPEED) - base) < 0.00001, "coffee immediately removes only fatigue");
            var duplicate = new ItemStack(ModItems.STRONG_COFFEE.get());
            duplicate.getItem().finishUsingItem(duplicate, level, owner);
            helper.assertTrue(duplicate.getCount() == 1, "no unnecessary repeat consumption");
            level.setDayTime(48000);
            NightwatchService.tickPlayer(owner);
            helper.assertTrue(NightwatchService.canClearFatigue(owner), "next day fatigue returns");
            NightwatchService.cleanup(owner);
            helper.assertTrue(Math.abs(owner.getAttributeValue(Attributes.MOVEMENT_SPEED) - base) < 0.00001, "cleanup leaves no stale modifier");
            helper.assertTrue(NightwatchSavedData.load(NightwatchSavedData.get(level).save(new CompoundTag()))
                    .find(owner.getUUID()).stacks == 3, "earned stack survives data round trip");
        } finally { level.setDayTime(before); NightwatchService.cleanup(owner); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchCoffeeMortarReturnsContainerWithoutDrinking(GameTestHelper helper) {
        var owner = player(helper, false);
        var level = helper.getLevel();
        var position = owner.blockPosition();
        level.setBlock(position, ModBlocks.MYSTIC_MORTAR.get().defaultBlockState(), 3);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.STRONG_COFFEE.get()));
        owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.SHADOW_MUSHROOM.get()));
        var state = level.getBlockState(position);
        state.use(level, owner, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(position), Direction.UP, position, false));
        helper.assertTrue(owner.getInventory().countItem(ModItems.NIGHTWATCH_EXTRACT.get()) == 1, "commoner can prepare extract");
        helper.assertTrue(owner.getInventory().countItem(Items.GLASS_BOTTLE) == 1, "mortar returns coffee bottle exactly once");
        helper.assertTrue(owner.getInventory().countItem(ModItems.SHADOW_MUSHROOM.get()) == 0, "mushroom consumed");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchAttackerCreditRejectsDuplicatesYoungAndPassiveMobs(GameTestHelper helper) {
        var level = helper.getLevel();
        long before = level.getDayTime();
        var owner = player(helper, true);
        var progress = NightwatchSavedData.get(level).getOrCreate(owner.getUUID());
        var owl = ModEntities.NIGHT_OWL.get().create(level);
        try {
            level.setDayTime(13000);
            progress.observe(13000, level.getGameTime(), true);
            owl.setTarget(owner);
            owl.tickCount = 20;
            NightwatchService.onDeath(new LivingDeathEvent(owl, owner.damageSources().playerAttack(owner)));
            helper.assertTrue(progress.attackers() == 0, "fresh spawn cannot be farmed immediately");
            owl.tickCount = 250;
            owl.setTarget(null);
            NightwatchService.onDeath(new LivingDeathEvent(owl, owner.damageSources().playerAttack(owner)));
            helper.assertTrue(progress.attackers() == 0, "non-attacker does not count");
            owl.setTarget(owner);
            for (int repeat = 0; repeat < 4; repeat++) NightwatchService.onDeath(new LivingDeathEvent(owl, owner.damageSources().playerAttack(owner)));
            helper.assertTrue(progress.attackers() == 1, "repeated callback credits one unique attacker");
            MinecraftForge.EVENT_BUS.post(new PlayerSleepInBedEvent(owner, java.util.Optional.of(owner.blockPosition())));
            helper.assertTrue(progress.attackers() == 0 && progress.watchedTicks() == 0, "sleep request immediately interrupts watch");
        } finally { level.setDayTime(before); NightwatchService.cleanup(owner); owl.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchWakeWardHasCooldownAndNeverBlocksVoid(GameTestHelper helper) {
        var owner = player(helper, true);
        var attacker = EntityType.ZOMBIE.create(helper.getLevel());
        owner.setSleepingPos(owner.blockPosition());
        var attack = new LivingAttackEvent(owner, owner.damageSources().mobAttack(attacker), 6);
        NightwatchService.onAttack(attack);
        helper.assertTrue(attack.isCanceled() && !owner.isSleeping(), "sleeping attack wakes without initial damage");
        var followup = new LivingHurtEvent(owner, owner.damageSources().mobAttack(attacker), 6);
        NightwatchService.onHurt(followup);
        helper.assertTrue(followup.isCanceled(), "one second ward");
        var voidDamage = new LivingHurtEvent(owner, owner.damageSources().fellOutOfWorld(), 6);
        NightwatchService.onHurt(voidDamage);
        helper.assertTrue(!voidDamage.isCanceled(), "void bypasses ward");
        owner.setSleepingPos(owner.blockPosition());
        var repeated = new LivingAttackEvent(owner, owner.damageSources().mobAttack(attacker), 6);
        NightwatchService.onAttack(repeated);
        helper.assertTrue(!repeated.isCanceled(), "wake cooldown prevents repeated invulnerability");
        var progress = NightwatchSavedData.get(helper.getLevel()).find(owner.getUUID());
        progress.wardUntil = helper.getLevel().getGameTime();
        var expired = new LivingHurtEvent(owner, owner.damageSources().mobAttack(attacker), 6);
        NightwatchService.onHurt(expired);
        helper.assertTrue(!expired.isCanceled(), "ward expires at exact boundary");
        progress.wardUntil = Long.MAX_VALUE;
        var corrupt = new LivingHurtEvent(owner, owner.damageSources().mobAttack(attacker), 6);
        NightwatchService.onHurt(corrupt);
        helper.assertTrue(!corrupt.isCanceled(), "corrupt persisted timestamp cannot grant infinite immunity");
        owner.clearSleepingPos();
        attacker.discard();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchCreativeSearchIncludesEveryRegisteredItem(GameTestHelper helper) {
        var tab = top.aurora.lordofmysteries.registry.ModCreativeTabs.MAIN_TAB.get();
        tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
        var displayed = tab.getDisplayItems().stream().map(ItemStack::getItem).collect(java.util.stream.Collectors.toSet());
        for (var entry : ModItems.ITEMS.getEntries()) {
            helper.assertTrue(displayed.contains(entry.get()), "creative/JEI discovery missing registered item " + entry.getId());
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchCoffeeCraftingAndRoastingUseRealRecipeManager(GameTestHelper helper) {
        var owner = player(helper, false);
        var crafting = new net.minecraft.world.inventory.TransientCraftingContainer(
                new net.minecraft.world.inventory.CraftingMenu(0, owner.getInventory()), 3, 3);
        crafting.setItem(0, new ItemStack(ModItems.COFFEE_BEANS.get()));
        crafting.setItem(1, new ItemStack(ModItems.COFFEE_BEANS.get()));
        crafting.setItem(2, new ItemStack(Items.SUGAR));
        crafting.setItem(3, new ItemStack(Items.WATER_BUCKET));
        crafting.setItem(4, new ItemStack(Items.GLASS_BOTTLE));
        var manager = helper.getLevel().getRecipeManager();
        var recipe = manager.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, crafting, helper.getLevel()).orElseThrow();
        helper.assertTrue(recipe.assemble(crafting, helper.getLevel().registryAccess()).is(ModItems.STRONG_COFFEE.get()), "coffee recipe is discoverable and craftable");
        helper.assertTrue(recipe.getRemainingItems(crafting).get(3).is(Items.BUCKET), "water bucket leaves one empty bucket");
        crafting.setItem(1, ItemStack.EMPTY);
        helper.assertTrue(!recipe.matches(crafting, helper.getLevel()), "one bean cannot craft a two-bean coffee");
        var campfire = new net.minecraft.world.SimpleContainer(new ItemStack(Items.COCOA_BEANS));
        var roasting = manager.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, campfire, helper.getLevel()).orElseThrow();
        helper.assertTrue(roasting.getCookingTime() == 600
                && roasting.assemble(campfire, helper.getLevel().registryAccess()).is(ModItems.COFFEE_BEANS.get()), "real thirty-second survival fallback loads");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void nightwatchStepsAffectOnlyNightFootsteps(GameTestHelper helper) {
        var level = helper.getLevel();
        long before = level.getDayTime();
        var owner = player(helper, true);
        try {
            level.setDayTime(14000);
            var step = new PlayLevelSoundEvent.AtEntity(owner, Holder.direct(SoundEvents.STONE_STEP), SoundSource.PLAYERS, 1, 1);
            NightwatchService.onStepSound(step);
            helper.assertTrue(Math.abs(step.getNewVolume() - 0.3f) < 0.00001, "night step volume reduced seventy percent");
            var hurt = new PlayLevelSoundEvent.AtEntity(owner, Holder.direct(SoundEvents.PLAYER_HURT), SoundSource.PLAYERS, 1, 1);
            NightwatchService.onStepSound(hurt);
            helper.assertTrue(hurt.getNewVolume() == 1, "combat cue stays audible");
            level.setDayTime(24000);
            var day = new PlayLevelSoundEvent.AtEntity(owner, Holder.direct(SoundEvents.STONE_STEP), SoundSource.PLAYERS, 1, 1);
            NightwatchService.onStepSound(day);
            helper.assertTrue(day.getNewVolume() == 1, "daytime step stays normal");
        } finally { level.setDayTime(before); }
        helper.succeed();
    }
}
