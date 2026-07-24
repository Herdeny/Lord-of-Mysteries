package top.aurora.lordofmysteries.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.characteristic.CharacteristicBundle;
import top.aurora.lordofmysteries.characteristic.CharacteristicConservationService;
import top.aurora.lordofmysteries.entity.SeerBreakdownEntity;
import top.aurora.lordofmysteries.knowledge.M1TrialProgress;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerCapabilityEvents;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.PotionQuality;
import top.aurora.lordofmysteries.characteristic.CharacteristicLedger;
import top.aurora.lordofmysteries.commission.InvestigationBoardService;
import top.aurora.lordofmysteries.registry.ModBlocks;
import top.aurora.lordofmysteries.registry.ModEntities;

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
        helper.assertTrue(saved.getList("migration_history", 10).size() == 5,
                "provider must persist the applied migration history");
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
}
