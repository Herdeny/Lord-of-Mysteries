package top.aurora.lordofmysteries.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.aurora.lordofmysteries.nightwatch.DreamCrystalHarvest;
import top.aurora.lordofmysteries.nightwatch.MidnightPoetService;
import top.aurora.lordofmysteries.nightwatch.NightwatchSavedData;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.registry.ModItems;

@PrefixGameTestTemplate(false)
public final class MidnightPoetGameTests {
    private MidnightPoetGameTests() {}

    private static ServerPlayer poet(GameTestHelper helper) {
        ServerPlayer player = NightwatchGameTests.player(helper, true);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(3, 12, 3))));
        var data = MysteryCapability.get(player);
        data.sequence = 8;
        data.spirituality = data.spiritualityMax = 140;
        for (int east = 0; east < 12; east++) {
            for (int south = 0; south < 12; south++) {
                helper.getLevel().setBlock(helper.absolutePos(new BlockPos(east, 11, south)), Blocks.STONE.defaultBlockState(), 3);
                for (int height = 12; height < 17; height++) {
                    helper.getLevel().setBlock(helper.absolutePos(new BlockPos(east, height, south)), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        return player;
    }

    private static Zombie zombie(GameTestHelper helper, ServerPlayer player) {
        var zombie = EntityType.ZOMBIE.create(helper.getLevel());
        zombie.setPos(player.position().add(2, -0.5, 0));
        zombie.setOnGround(true);
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void poetReagentChainAndRealCrucibleProduceAdvancementPotion(GameTestHelper helper) {
        var owner = poet(helper);
        var level = helper.getLevel();
        var position = owner.blockPosition();
        var mortar = top.aurora.lordofmysteries.registry.ModBlocks.MYSTIC_MORTAR.get().defaultBlockState();
        level.setBlock(position, mortar, 3);
        var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(position), net.minecraft.core.Direction.UP, position, false);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FEATHER));
        owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.INK_SAC));
        mortar.use(level, owner, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(owner.getInventory().countItem(ModItems.RAVEN_INK_QUILL.get()) == 1, "first actual mortar recipe");
        owner.getInventory().clearContent();
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.RAVEN_INK_QUILL.get()));
        owner.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.HEATHER.get()));
        mortar.use(level, owner, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(owner.getInventory().countItem(ModItems.POET_REAGENT.get()) == 1, "second actual mortar recipe");
        level.setBlock(position.below(), Blocks.CAMPFIRE.defaultBlockState(), 3);
        level.setBlock(position, top.aurora.lordofmysteries.registry.ModBlocks.CRUCIBLE.get().defaultBlockState(), 3);
        var crucible = (top.aurora.lordofmysteries.potion.CrucibleBlockEntity) level.getBlockEntity(position);
        for (var item : new net.minecraft.world.item.Item[]{ModItems.DREAM_MURMUR_CRYSTAL.get(), ModItems.POET_REAGENT.get(), ModItems.SPIRIT_SALT.get()}) {
            var stack = new ItemStack(item);
            helper.assertTrue(crucible.insert(stack, owner) && stack.isEmpty(), "exact ingredient insertion");
        }
        helper.assertTrue(crucible.startBrewing(), "actual recipe starts");
        for (int tick = 0; tick < 1200; tick++) top.aurora.lordofmysteries.potion.CrucibleBlockEntity.serverTick(level, position, level.getBlockState(position), crucible);
        helper.assertTrue(crucible.takeResult().is(ModItems.SLEEPLESS_POTION_8.get()), "actual potion output");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void poetAlliesAndExclusiveActivitiesNeverConsumeSpirit(GameTestHelper helper) {
        var owner = poet(helper);
        var zombie = zombie(helper, owner);
        var board = helper.getLevel().getScoreboard();
        var name = "poet-" + owner.getUUID().toString().substring(0, 8);
        var team = board.addPlayerTeam(name);
        board.addPlayerToTeam(owner.getScoreboardName(), team);
        board.addPlayerToTeam(zombie.getScoreboardName(), team);
        var routes = top.aurora.lordofmysteries.spirit.SpiritExpeditionSavedData.get(helper.getLevel());
        try {
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, zombie), "same-team enemy excluded");
            board.removePlayerFromTeam(zombie.getScoreboardName(), team);
            routes.start(owner.getUUID(), helper.getLevel().dimension().location(), owner.blockPosition(),
                    top.aurora.lordofmysteries.spirit.SpiritProjection.THEATRE, 11L, helper.getLevel().getGameTime());
            helper.assertTrue(!MidnightPoetService.murmur(owner) && !MidnightPoetService.sleepTarget(owner, zombie), "exclusive activity gate first");
            helper.assertTrue(MysteryCapability.get(owner).spirituality == 140, "resources unchanged");
        } finally {
            routes.remove(owner.getUUID());
            board.removePlayerTeam(team);
            MidnightPoetService.cleanup(owner);
            zombie.discard();
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void poetProgressionRejectsInvalidDigestionAndPreservesIdentity(GameTestHelper helper) {
        var owner = NightwatchGameTests.player(helper, true);
        var data = MysteryCapability.get(owner);
        var potion = new ItemStack(ModItems.SLEEPLESS_POTION_8.get());
        for (float digestion : new float[]{0, 99.9f, Float.NaN, Float.POSITIVE_INFINITY}) {
            data.digestion = digestion;
            potion.getItem().finishUsingItem(potion, helper.getLevel(), owner);
            helper.assertTrue(potion.getCount() == 1 && data.sequence == 9, "invalid digestion does not consume or advance");
        }
        data.digestion = 100;
        potion.getItem().finishUsingItem(potion, helper.getLevel(), owner);
        helper.assertTrue(data.sequence == 8 && data.spiritualityMax == 140 && potion.isEmpty(), "exact sequence and spirit");
        helper.assertTrue(owner.getInventory().countItem(ModItems.ACTING_IDENTITY_CARD.get()) == 0, "no second first-potion kit");
        var skipped = new ItemStack(ModItems.SLEEPLESS_POTION_8.get());
        skipped.getItem().finishUsingItem(skipped, helper.getLevel(), owner);
        helper.assertTrue(skipped.getCount() == 1, "cannot repeat sequence 8");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top", timeoutTicks = 220)
    public static void poemExpiresWithoutFreezingAiOrPersistingControl(GameTestHelper helper) {
        var owner = poet(helper);
        var zombie = zombie(helper, owner);
        try {
            helper.assertTrue(MidnightPoetService.sleepTarget(owner, zombie), "real enemy accepts poem");
            helper.assertTrue(MidnightPoetService.sleeping(zombie) && !zombie.isNoAi(), "sleep does not alter NoAI");
            helper.assertTrue(MysteryCapability.get(owner).spirituality == 125, "exact 15 cost");
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, zombie), "duplicate is rejected");
            helper.assertTrue(MysteryCapability.get(owner).spirituality == 125, "duplicate costs nothing");
        } catch (Throwable failure) {
            MidnightPoetService.cleanup(owner);
            zombie.discard();
            throw failure;
        }
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(zombie.getTarget() == null && zombie.getNavigation().isDone(), "real AI target and navigation stay stopped");
            var attack = new net.minecraftforge.event.entity.living.LivingAttackEvent(owner,
                    helper.getLevel().damageSources().mobAttack(zombie), 3);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(attack);
            helper.assertTrue(attack.isCanceled(), "sleeping mob cannot deal melee damage");
        });
        helper.runAfterDelay(170, () -> {
            helper.assertTrue(!MidnightPoetService.sleeping(zombie) && !zombie.isNoAi(), "normal expiry releases AI");
            MidnightPoetService.cleanup(owner);
            zombie.discard();
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void poemDamageWakesAndOtherCasterCannotChain(GameTestHelper helper) {
        var owner = poet(helper);
        var other = NightwatchGameTests.player(helper, true);
        other.setPos(owner.position());
        MysteryCapability.get(other).sequence = 8;
        var zombie = zombie(helper, owner);
        try {
            helper.assertTrue(MidnightPoetService.sleepTarget(owner, zombie), "sleep succeeds");
            helper.assertTrue(!MidnightPoetService.sleepTarget(other, zombie), "another caster cannot steal control");
            zombie.hurt(helper.getLevel().damageSources().generic(), 1);
            helper.assertTrue(!MidnightPoetService.sleeping(zombie), "actual damage wakes");
            helper.assertTrue(!MidnightPoetService.sleepTarget(other, zombie), "shared 20-second immunity survives wake");
            helper.assertTrue(MysteryCapability.get(other).spirituality == 114, "other player not charged");
        } finally { MidnightPoetService.cleanup(owner); MidnightPoetService.cleanup(other); zombie.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void poetRejectsPlayersBossesInvisibleAndActivityTargets(GameTestHelper helper) {
        var owner = poet(helper);
        var other = NightwatchGameTests.player(helper, true);
        other.setPos(owner.position());
        var zombie = zombie(helper, owner);
        var boss = EntityType.WARDEN.create(helper.getLevel());
        boss.setPos(zombie.position());
        var villager = EntityType.VILLAGER.create(helper.getLevel());
        villager.setPos(zombie.position());
        try {
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, other), "players never controlled");
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, boss), "warden immune");
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, villager), "villager immune");
            zombie.setInvisible(true);
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, zombie), "invisible targets stay hidden");
            zombie.setInvisible(false);
            zombie.getPersistentData().putUUID(top.aurora.lordofmysteries.ability.MarionetteService.OWNER_TAG, other.getUUID());
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, zombie), "owned marionette excluded even without a team");
            zombie.getPersistentData().putString(top.aurora.lordofmysteries.ability.MarionetteService.OWNER_TAG, "malformed");
            helper.assertTrue(!MidnightPoetService.validTarget(owner, zombie), "malformed ownership fails closed");
            zombie.getPersistentData().remove(top.aurora.lordofmysteries.ability.MarionetteService.OWNER_TAG);
            var ecology = top.aurora.lordofmysteries.registry.ModEntities.MEMORY_LEECH.get().create(helper.getLevel());
            ecology.setPos(zombie.position());
            helper.assertTrue(!MidnightPoetService.validTarget(owner, ecology), "personal spirit encounter excluded");
            MysteryCapability.get(owner).spirituality = Float.NaN;
            helper.assertTrue(!MidnightPoetService.murmur(owner), "NaN cannot fund abilities");
            MysteryCapability.get(owner).spirituality = 140;
            owner.getPersistentData().putBoolean("qa_spectator", true);
            helper.assertTrue(!MidnightPoetService.sleepTarget(owner, zombie), "spectator cannot cast");
            helper.assertTrue(MysteryCapability.get(owner).spirituality == 140, "denials never charge");
        } finally { MidnightPoetService.cleanup(owner); zombie.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void murmurAccumulatesFearAndPersistsCooldown(GameTestHelper helper) {
        var owner = poet(helper);
        var zombie = zombie(helper, owner);
        try {
            helper.assertTrue(MidnightPoetService.murmur(owner), "first murmur");
            helper.assertTrue(MidnightPoetService.fear(zombie) == 2, "two fear");
            helper.assertTrue(MysteryCapability.get(owner).spirituality == 122, "exact 18 spirit");
            helper.assertTrue(!MidnightPoetService.murmur(owner), "cooldown rejects repeat");
            helper.assertTrue(MidnightPoetService.fear(zombie) == 2, "denied repeat does not stack");
            var saved = NightwatchSavedData.get(helper.getLevel());
            var snapshot = saved.save(new net.minecraft.nbt.CompoundTag());
            helper.assertTrue(NightwatchSavedData.load(snapshot).find(owner.getUUID()).murmurCooldown
                    == helper.getLevel().getGameTime() + 500, "cooldown survives save/reload");
            saved.getOrCreate(owner.getUUID()).murmurCooldown = 0;
            helper.assertTrue(MidnightPoetService.murmur(owner), "second allowed cast");
            helper.assertTrue(MidnightPoetService.fear(zombie) == 4, "four fear, not yet routed");
            MidnightPoetService.cleanup(owner);
            helper.assertTrue(MidnightPoetService.fear(zombie) == 0 && !zombie.isNoAi(), "logout releases all transient control");
        } finally { MidnightPoetService.cleanup(owner); zombie.discard(); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void thirdMurmurUsesRealPathsAndRewardsOnlyActualRouts(GameTestHelper helper) {
        var owner = poet(helper);
        var mobs = new java.util.ArrayList<Zombie>();
        for (int index = 0; index < 3; index++) {
            var mob = zombie(helper, owner);
            mob.setPos(owner.position().add(2, -0.5, index - 1));
            mob.getRandom().setSeed(108 + index);
            mobs.add(mob);
        }
        try {
            var data = MysteryCapability.get(owner);
            data.digestion = 0;
            for (int cast = 0; cast < 3; cast++) {
                NightwatchSavedData.get(helper.getLevel()).getOrCreate(owner.getUUID()).murmurCooldown = 0;
                helper.assertTrue(MidnightPoetService.murmur(owner), "valid cast " + cast);
                if (cast < 2) helper.assertTrue(data.digestion == 0, "no reward below fear threshold");
            }
            long moving = mobs.stream().filter(mob -> !mob.getNavigation().isDone()).count();
            helper.assertTrue(moving == 3, "all three must find real navigation, not a cosmetic fear counter: " + mobs.stream().map(mob -> MidnightPoetService.fear(mob) + "/ground=" + mob.onGround()).toList());
            helper.assertTrue((moving >= 3) == (data.digestion > 0), "acting exactly matches at least three actual rout paths");
        } finally { MidnightPoetService.cleanup(owner); mobs.forEach(Zombie::discard); }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "igloo/top")
    public static void dreamHarvestIsSharedAcrossPlayersAndCannotTimeSkip(GameTestHelper helper) {
        var owner = poet(helper);
        var other = NightwatchGameTests.player(helper, false);
        other.setPos(owner.position());
        owner.setShiftKeyDown(true);
        other.setShiftKeyDown(true);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.AMETHYST_SHARD, 2));
        other.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.AMETHYST_SHARD, 2));
        var villager = EntityType.VILLAGER.create(helper.getLevel());
        villager.setPos(owner.position().add(1, 0, 0));
        villager.setSleepingPos(villager.blockPosition());
        long time = helper.getLevel().getDayTime();
        try {
            helper.getLevel().setDayTime(13000);
            helper.assertTrue(DreamCrystalHarvest.harvest(owner, villager, InteractionHand.MAIN_HAND), "sleeping adult yields crystal");
            helper.assertTrue(villager.isSleeping() && owner.getMainHandItem().getCount() == 1, "sleep preserved, one shard consumed");
            helper.assertTrue(owner.getInventory().countItem(ModItems.DREAM_MURMUR_CRYSTAL.get()) == 1, "one actual crystal");
            helper.assertTrue(!DreamCrystalHarvest.harvest(other, villager, InteractionHand.MAIN_HAND), "second player shares cap");
            helper.getLevel().setDayTime(37000);
            helper.assertTrue(!DreamCrystalHarvest.harvest(other, villager, InteractionHand.MAIN_HAND), "time command cannot skip actual 24000-tick wait");
            helper.assertTrue(other.getMainHandItem().getCount() == 2, "denial preserves second player's shards");
        } finally { helper.getLevel().setDayTime(time); }
        helper.succeed();
    }
}
