package top.aurora.lordofmysteries.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.ability.M2FoundationAbilityHandler;
import top.aurora.lordofmysteries.ability.M3LaunchAbilityHandler;
import top.aurora.lordofmysteries.characteristic.CharacteristicBundle;
import top.aurora.lordofmysteries.characteristic.CharacteristicConservationService;
import top.aurora.lordofmysteries.entity.SeerBreakdownEntity;
import top.aurora.lordofmysteries.knowledge.M1TrialProgress;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerCapabilityEvents;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.PotionQuality;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.characteristic.CharacteristicLedger;
import top.aurora.lordofmysteries.commission.InvestigationBoardService;
import top.aurora.lordofmysteries.registry.ModBlocks;
import top.aurora.lordofmysteries.registry.ModEntities;
import top.aurora.lordofmysteries.registry.ModItems;

@PrefixGameTestTemplate(false)
public final class PlayerPersistenceGameTests {

    private static final String TEMPLATE_NAMESPACE = "minecraft";
    private static final String TEMPLATE = "igloo/top";

    private PlayerPersistenceGameTests() {}

    public static void register(RegisterGameTestsEvent event) {
        event.register(PlayerPersistenceGameTests.class);
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void capabilityClonePreservesLifecycleState(
            GameTestHelper helper) {
        ServerPlayer original = createPlayer(helper, "original");
        ServerPlayer clone = createPlayer(helper, "clone");
        PlayerMysteryData originalData = original.getCapability(
                MysteryCapability.MYSTERY_DATA).resolve().orElse(null);
        helper.assertTrue(originalData != null,
                "mock server player must receive mystery capability");
        if (originalData == null) return;

        originalData.pathway = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "seer");
        originalData.sequence = 7;
        originalData.spirituality = 63f;
        originalData.paperSubstituteArmedEndTick = 900L;
        originalData.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "guide/awakening"));
        originalData.moneyPence = 345L;
        originalData.m1TrialActive = true;
        originalData.m1TrialSessionId =
                "d21fbf02-2d69-4e09-ac2f-0a8e0f71d79d";

        PlayerCapabilityEvents.ForgeBus.onPlayerClone(
                new PlayerEvent.Clone(clone, original, true));

        PlayerMysteryData cloneData = clone.getCapability(
                MysteryCapability.MYSTERY_DATA).resolve().orElse(null);
        helper.assertTrue(cloneData != null,
                "cloned player must retain mystery capability");
        if (cloneData == null) return;
        helper.assertTrue(originalData.pathway.equals(cloneData.pathway),
                "pathway must survive death/respawn clone");
        helper.assertTrue(cloneData.sequence == 7,
                "sequence must survive death/respawn clone");
        helper.assertTrue(cloneData.paperSubstituteArmedEndTick == 900L,
                "ability cooldown state must survive clone");
        helper.assertTrue(cloneData.knownKnowledge.equals(
                        originalData.knownKnowledge),
                "knowledge state must survive clone");
        helper.assertTrue(cloneData.moneyPence == 345L,
                "social state must survive clone");
        helper.assertTrue(cloneData.m1TrialActive
                        && "d21fbf02-2d69-4e09-ac2f-0a8e0f71d79d"
                        .equals(cloneData.m1TrialSessionId),
                "continuity state must survive clone");
        helper.assertTrue(cloneData.dirtySectionMask()
                        == PlayerDataSection.ALL_MASK,
                "cloned state must request a full initial synchronization");
        helper.succeed();
    }

    private static ServerPlayer createPlayer(GameTestHelper helper,
                                             String name) {
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "gametest-" + name));
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void providerRoundTripMigratesSchemaFifteen(
            GameTestHelper helper) {
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("schema_version", 15);
        legacy.putString("pathway", ProjectMystery.MOD_ID + ":seer");
        legacy.putInt("sequence", 8);
        legacy.putFloat("spirituality", 44f);
        legacy.putFloat("spirituality_max", 100f);
        legacy.putString("potion_quality", "complete");

        MysteryCapability.Provider provider = new MysteryCapability.Provider();
        provider.deserializeNBT(legacy);
        PlayerMysteryData data = provider.getData();
        CompoundTag saved = provider.serializeNBT();

        helper.assertTrue(data.schemaVersion
                        == PlayerMysteryData.CURRENT_SCHEMA_VERSION,
                "schema 15 must migrate to the current schema");
        helper.assertTrue(data.characteristicBundles.size() == 1,
                "legacy pathway state must become a characteristic bundle");
        helper.assertTrue(data.migrationBackups.size() == 1,
                "migration must preserve one recovery backup");
        helper.assertTrue(saved.getInt("schema_version")
                        == PlayerMysteryData.CURRENT_SCHEMA_VERSION,
                "provider must persist the migrated schema");
        helper.assertTrue(saved.getList("migration_history", 10).size()
                        == PlayerMysteryData.CURRENT_SCHEMA_VERSION - 15,
                "provider must persist the applied migration history");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void cityEconomyAndExposureSurviveServerRoundTrip(
            GameTestHelper helper) {
        MysteryCapability.Provider source =
                new MysteryCapability.Provider();
        PlayerMysteryData sourceData = source.getData();
        sourceData.moneyPence = 71L;
        sourceData.lastCityWorkDay = 9L;
        sourceData.cityWorkShifts = 6;
        sourceData.pressWorkShifts = 2;
        sourceData.agencyWorkShifts = 3;
        sourceData.patrolWorkShifts = 1;
        sourceData.mysticalExposure = 42f;

        MysteryCapability.Provider restored =
                new MysteryCapability.Provider();
        restored.deserializeNBT(source.serializeNBT());
        PlayerMysteryData restoredData = restored.getData();

        helper.assertTrue(restoredData.moneyPence == 71L,
                "city currency must survive the server provider round trip");
        helper.assertTrue(restoredData.cityWorkShifts == 6
                        && restoredData.pressWorkShifts == 2
                        && restoredData.agencyWorkShifts == 3
                        && restoredData.patrolWorkShifts == 1,
                "all city job counters must survive restart");
        helper.assertTrue(restoredData.mysticalExposure == 42f,
                "mystical exposure must survive restart");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void futureSchemaIsQuarantinedWithoutDataLoss(
            GameTestHelper helper) {
        CompoundTag future = new CompoundTag();
        future.putInt("schema_version", 99);
        future.putString("future_only_field", "keep-me");

        MysteryCapability.Provider provider = new MysteryCapability.Provider();
        provider.deserializeNBT(future);
        PlayerMysteryData data = provider.getData();

        helper.assertTrue(data.futureSchemaDetected,
                "future schema must be reported");
        helper.assertTrue(data.orphanedEntries.size() == 1,
                "future payload must be quarantined for recovery");
        CompoundTag payload = data.orphanedEntries.get(0)
                .getCompound("payload");
        helper.assertTrue("keep-me".equals(
                        payload.getString("future_only_field")),
                "future-only fields must remain recoverable");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void characteristicBundleSurvivesCarrierAndItemRoundTrip(
            GameTestHelper helper) {
        PlayerMysteryData data = new PlayerMysteryData();
        data.pathway = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "seer");
        data.sequence = 8;
        CharacteristicLedger.recordPotionAdvancement(
                data, data.pathway, 9, PotionQuality.COMPLETE);
        CharacteristicLedger.recordPotionAdvancement(
                data, data.pathway, 8, PotionQuality.PERFECT);
        CharacteristicBundle expected = data.characteristicBundles.get(0);
        SeerBreakdownEntity carrier = ModEntities.SEER_BREAKDOWN.get().create(
                helper.getLevel());
        helper.assertTrue(carrier != null,
                "characteristic carrier entity must be constructible");
        if (carrier == null) return;
        helper.assertTrue(CharacteristicConservationService.transferCurrentBundle(
                        carrier, data),
                "current pathway bundle must transfer to the carrier");
        helper.assertTrue(data.characteristicBundles.isEmpty(),
                "transferred bundle must leave the player ledger exactly once");
        CharacteristicBundle carried = CharacteristicConservationService
                .readCarrier(carrier).orElse(null);
        helper.assertTrue(expected.equals(carried),
                "carrier must retain the exact layered characteristic payload");
        ItemStack stack = CharacteristicConservationService.createStack(expected);
        helper.assertTrue(expected.equals(CharacteristicConservationService
                        .readStack(stack).orElse(null)),
                "broken characteristic item must retain the exact payload");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void twoHourM1EvidenceSurvivesProviderRoundTrip(
            GameTestHelper helper) {
        PlayerMysteryData source = new PlayerMysteryData();
        source.m1TrialElapsedTicks = M1TrialProgress.REQUIRED_TICKS;
        source.m1TrialCampVisited = true;
        source.m1TrialBestSequence = 7;
        source.m1TrialOccultKills = M1TrialProgress.REQUIRED_OCCULT_KILLS;
        source.m1TrialActingEvents = M1TrialProgress.REQUIRED_ACTING_EVENTS;
        source.m1TrialMaxPressure = M1TrialProgress.REQUIRED_RISK_PEAK;
        source.identityAnchored = true;
        source.actingReflectionCount = 1;
        source.cityWorkShifts = 1;
        source.lastCityWorkDay = 4L;
        source.m1TrialIdentityAnchoredTick = 120000L;
        source.m1TrialReflectionCompletedTick = 132000L;
        source.m1TrialStreetLifeCompletedTick = 144000L;

        MysteryCapability.Provider provider = new MysteryCapability.Provider();
        provider.deserializeNBT(source.save());
        PlayerMysteryData restored = provider.getData();
        M1TrialProgress.Result result = M1TrialProgress.evaluate(
                restored.m1TrialElapsedTicks, restored.m1TrialCampVisited,
                restored.m1TrialBestSequence, restored.m1TrialOccultKills,
                restored.m1TrialActingEvents, restored.m1TrialMaxPressure,
                restored.m1TrialMaxPollution, restored.identityAnchored,
                restored.actingReflectionCount > 0,
                restored.cityWorkShifts > 0);
        helper.assertTrue(result.passed(),
                "all nine M1 goals must survive provider serialization");
        helper.assertTrue(restored.m1TrialStreetLifeCompletedTick == 144000L,
                "two-hour street-life milestone must survive restart state");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void investigationBoardRequiresPhysicalProximity(
            GameTestHelper helper) {
        BlockPos board = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(
                board, ModBlocks.COMMISSION_BOARD.get().defaultBlockState());
        ServerPlayer player = createPlayer(helper, "board-proximity");
        player.setPos(board.getX() + 2.5d, board.getY(), board.getZ() + 0.5d);

        helper.assertTrue(InvestigationBoardService.isNearBoard(player),
                "server must allow board actions while the player is nearby");

        player.setPos(board.getX() + 20.5d, board.getY(), board.getZ() + 0.5d);
        helper.assertTrue(!InvestigationBoardService.isNearBoard(player),
                "server must reject remote board actions");
        helper.succeed();
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void fiveM3PotionChainsAdvanceAndPersist(
            GameTestHelper helper) {
        verifyM3PotionChain(helper, "seer",
                ModItems.SEER_POTION_6.get(), ModItems.SEER_POTION_5.get(),
                212f, 265f);
        verifyM3PotionChain(helper, "spectator",
                ModItems.SPECTATOR_POTION_6.get(), ModItems.SPECTATOR_POTION_5.get(),
                205f, 258f);
        verifyM3PotionChain(helper, "hunter",
                ModItems.HUNTER_POTION_6.get(), ModItems.HUNTER_POTION_5.get(),
                218f, 272f);
        verifyM3PotionChain(helper, "thief",
                ModItems.THIEF_POTION_6.get(), ModItems.THIEF_POTION_5.get(),
                198f, 246f);
        verifyM3PotionChain(helper, "apprentice",
                ModItems.APPRENTICE_POTION_6.get(),
                ModItems.APPRENTICE_POTION_5.get(),
                200f, 250f);
        helper.succeed();
    }

    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = TEMPLATE)
    public static void fiveM3PathwaysExecuteServerAuthoritativeAbilities(
            GameTestHelper helper) {
        long now = helper.getLevel().getGameTime();

        ServerPlayer seer = m3Player(helper, "ability-seer", "seer", 5);
        PlayerMysteryData seerData = MysteryCapability.get(seer);
        Mob seerTarget = spawnMob(helper, seer, 4d, 0d);
        helper.assertTrue(M3LaunchAbilityHandler.use(
                        seer, M2FoundationAbilityHandler.AbilitySlot.PRIMARY),
                "spirit-thread revelation must execute on the server");
        helper.assertTrue(seerData.spirituality == 65f
                        && seerTarget.hasEffect(MobEffects.GLOWING)
                        && seerData.airBulletCooldownEndTick > now,
                "spirit threads must consume spirit, reveal a real mob and start cooldown");
        seerTarget.discard();

        ServerPlayer spectator = m3Player(
                helper, "ability-spectator", "spectator", 6);
        PlayerMysteryData spectatorData = MysteryCapability.get(spectator);
        Mob spectatorTarget = spawnMob(helper, spectator, 0d, 4d);
        helper.assertTrue(M3LaunchAbilityHandler.use(
                        spectator, M2FoundationAbilityHandler.AbilitySlot.PRIMARY),
                "hypnotic command must execute on the server");
        helper.assertTrue(spectatorData.spirituality == 70f
                        && spectatorTarget.getTarget() == null
                        && spectatorData.psychPacifyCooldownEndTick > now,
                "hypnosis must consume spirit, de-escalate a real mob and start cooldown");
        spectatorTarget.discard();

        ServerPlayer hunter = m3Player(helper, "ability-hunter", "hunter", 6);
        PlayerMysteryData hunterData = MysteryCapability.get(hunter);
        Mob secondHunterTarget = spawnMob(helper, hunter, 4d, 4d);
        Mob firstHunterTarget = spawnMob(helper, hunter, 0d, 4d);
        helper.assertTrue(M3LaunchAbilityHandler.use(
                        hunter, M2FoundationAbilityHandler.AbilitySlot.SECONDARY),
                "conflict instigation must execute on the server");
        helper.assertTrue(hunterData.spirituality == 75f
                        && firstHunterTarget.getTarget() == secondHunterTarget
                        && secondHunterTarget.getTarget() == firstHunterTarget
                        && hunterData.pyroRingCooldownEndTick > now,
                "instigation must consume spirit, affect two real mobs and start cooldown");
        firstHunterTarget.discard();
        secondHunterTarget.discard();

        ServerPlayer thief = m3Player(helper, "ability-thief", "thief", 6);
        PlayerMysteryData thiefData = MysteryCapability.get(thief);
        ItemEntity unownedItem = new ItemEntity(
                helper.getLevel(), thief.getX() + 5d, thief.getY(),
                thief.getZ(), new ItemStack(Items.DIAMOND));
        helper.getLevel().addFreshEntity(unownedItem);
        helper.assertTrue(M3LaunchAbilityHandler.use(
                        thief, M2FoundationAbilityHandler.AbilitySlot.SECONDARY),
                "unowned-item retrieval must execute on the server");
        helper.assertTrue(thiefData.spirituality == 88f
                        && unownedItem.distanceToSqr(thief) < 2d
                        && thiefData.thiefLockpickCooldownEndTick > now,
                "retrieval must consume spirit and move only the eligible world item");

        ServerPlayer apprentice = m3Player(
                helper, "ability-apprentice", "apprentice", 6);
        PlayerMysteryData apprenticeData = MysteryCapability.get(apprentice);
        apprentice.setItemInHand(
                InteractionHand.MAIN_HAND, new ItemStack(Items.FILLED_MAP));
        helper.assertTrue(M3LaunchAbilityHandler.use(
                        apprentice, M2FoundationAbilityHandler.AbilitySlot.PRIMARY),
                "perfect copy must execute on the server");
        helper.assertTrue(apprenticeData.spirituality == 75f
                        && apprentice.getInventory().countItem(Items.FILLED_MAP) == 2
                        && apprenticeData.apprenticeRelocateCooldownEndTick > now,
                "copying must consume spirit, preserve the original and start cooldown");
        helper.succeed();
    }

    private static ServerPlayer m3Player(
            GameTestHelper helper, String name, String pathway, int sequence) {
        ServerPlayer player = createPlayer(helper, name);
        BlockPos start = helper.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(start.getX() + 0.5d, start.getY(), start.getZ() + 0.5d);
        PlayerMysteryData data = MysteryCapability.get(player);
        data.pathway = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, pathway);
        data.sequence = sequence;
        data.spiritualityMax = 100f;
        data.spirituality = 100f;
        return player;
    }

    private static Mob spawnMob(
            GameTestHelper helper, ServerPlayer player,
            double xOffset, double zOffset) {
        Mob mob = (Mob) EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(mob != null, "ability target mob must be constructible");
        if (mob == null) throw new IllegalStateException("zombie creation failed");
        mob.setPos(player.getX() + xOffset, player.getY(), player.getZ() + zOffset);
        mob.setNoAi(true);
        helper.getLevel().addFreshEntity(mob);
        player.setYRot((float) Math.toDegrees(
                Math.atan2(-xOffset, zOffset)));
        player.setXRot(0f);
        return mob;
    }

    private static void verifyM3PotionChain(
            GameTestHelper helper, String pathway, Item sequenceSixPotion,
            Item sequenceFivePotion, float sequenceSixSpirituality,
            float sequenceFiveSpirituality) {
        ServerPlayer player = createPlayer(helper, "m3-" + pathway);
        PlayerMysteryData data = MysteryCapability.get(player);
        data.pathway = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, pathway);
        data.sequence = 7;
        data.digestion = 100f;
        data.spiritualityMax = 180f;
        data.spirituality = 180f;

        ItemStack sequenceSix = SeerPotionItem.create(
                sequenceSixPotion, PotionQuality.COMPLETE);
        sequenceSixPotion.finishUsingItem(
                sequenceSix, helper.getLevel(), player);
        helper.assertTrue(data.sequence == 6,
                pathway + " must advance from sequence 7 to 6");
        helper.assertTrue(data.spiritualityMax == sequenceSixSpirituality,
                pathway + " sequence 6 spirituality must match v0.9");

        data.digestion = 100f;
        ItemStack sequenceFive = SeerPotionItem.create(
                sequenceFivePotion, PotionQuality.COMPLETE);
        sequenceFivePotion.finishUsingItem(
                sequenceFive, helper.getLevel(), player);
        helper.assertTrue(data.sequence == 5,
                pathway + " must advance from sequence 6 to 5");
        helper.assertTrue(data.spiritualityMax == sequenceFiveSpirituality,
                pathway + " sequence 5 spirituality must match v0.9");
        helper.assertTrue(data.characteristicBundles.size() == 1
                        && data.characteristicBundles.get(0).layers().size() == 2,
                pathway + " must conserve both newly consumed characteristics");

        MysteryCapability.Provider restored =
                new MysteryCapability.Provider();
        restored.deserializeNBT(data.save());
        PlayerMysteryData restoredData = restored.getData();
        helper.assertTrue(restoredData.pathway.equals(data.pathway)
                        && restoredData.sequence == 5
                        && restoredData.spiritualityMax == sequenceFiveSpirituality,
                pathway + " sequence 5 state must survive provider restart");
        helper.assertTrue(restoredData.characteristicBundles.equals(
                        data.characteristicBundles),
                pathway + " characteristic ledger must survive provider restart");
    }
}
