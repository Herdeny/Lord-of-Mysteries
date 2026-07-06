package top.aurora.lordofmysteries;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

/**
 * M0 基础单元测试框架（设计文档 §20 M0 任务：基础单元测试框架）。
 * 验证 PlayerMysteryData 默认值与普通人/非凡者判定逻辑。
 *
 * 注：test 源集默认不在 Minecraft 运行时类路径上（NeoForge ModDev），
 * 因此这里只覆盖不依赖 net.minecraft.* 的纯 POJO 逻辑。涉及 ResourceLocation /
 * Codec 反序列化 / 能力检定的测试放到 gameTestServer（§20 M0 任务）。
 *
 * <p>这些测试看起来很小，但它们保护的是玩家数据最底层的默认状态。默认值一旦变化，
 * 魔药服用、死亡复制、HUD 显示和服务端配置都可能出现连锁影响。
 */
class PlayerMysteryDataTest {

    /** 新建数据必须代表“普通人”，否则新玩家进服会直接被当作非凡者处理。 */
    @Test
    void defaultsAreCommoner() {
        PlayerMysteryData d = new PlayerMysteryData();
        assertNull(d.pathway, "默认应为普通人（pathway=null）");
        assertEquals(-1, d.sequence, "默认序列应为 -1");
        assertFalse(d.isExtraordinary(), "默认不应为非凡者");
        assertEquals(0f, d.spirituality);
        assertEquals(100f, d.spiritualityMax);
        assertEquals(0f, d.pollution);
        assertEquals(PlayerMysteryData.CURRENT_SCHEMA_VERSION, d.schemaVersion);
        assertFalse(d.emotionReadActive);
        assertEquals("", d.hunterTrackedTarget);
        assertEquals(0L, d.provokeCooldownEndTick);
        assertEquals(0L, d.enrageCooldownEndTick);
        assertEquals(0L, d.battleWillCooldownEndTick);
        assertEquals(0L, d.paperSubstituteArmedEndTick);
        assertEquals("", d.paperSubstituteDimension);
        assertEquals(0, d.clownDodgeCount);
        assertEquals(Long.MIN_VALUE, d.lastRestRecoveryDay);
        assertFalse(d.m1TrialActive);
        assertEquals(-1, d.m1TrialBestSequence);
        assertEquals(0L, d.m1TrialElapsedTicks);
        assertTrue(d.actingCounters.isEmpty());
    }

    /** 只有序列值不足以成为非凡者，必须同时拥有途径 ID。 */
    @Test
    void notExtraordinaryWithoutPathwayEvenIfSequenceSet() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.sequence = 9; // 仅有序列、无途径 → 仍非非凡者
        assertFalse(d.isExtraordinary(), "无途径时不应判定为非凡者");
    }

    /** 负数序列代表未入途径，防止边界值误判。 */
    @Test
    void sequenceBoundaryStaysCommonerWhenNegative() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.sequence = -1;
        assertFalse(d.isExtraordinary());
    }

    @Test
    void sequenceSevenAbilityStateSurvivesCapabilityCopy() {
        PlayerMysteryData source = new PlayerMysteryData();
        source.paperSubstituteArmedEndTick = 900L;
        source.paperSubstituteDimension = "minecraft:overworld";
        source.paperSubstituteX = 12.5d;
        source.paperSubstituteY = 70d;
        source.paperSubstituteZ = -4.5d;
        source.flameLeapCooldownEndTick = 400L;
        source.clownDodgeCount = 4;
        source.lastRestRecoveryDay = 12L;
        source.m1TrialActive = true;
        source.m1TrialStartTick = 100L;
        source.m1TrialElapsedTicks = 200L;
        source.m1TrialCampVisited = true;
        source.m1TrialBestSequence = 7;
        source.m1TrialOccultKills = 3;
        source.m1TrialActingEvents = 2;
        source.m1TrialMaxPressure = 42f;
        source.thiefPilferCooldownEndTick = 300L;
        source.thiefEscapeCooldownEndTick = 600L;
        source.apprenticeTrickCooldownEndTick = 200L;
        source.apprenticeCopyCooldownEndTick = 400L;
        source.psychPacifyCooldownEndTick = 500L;
        source.psychShockCooldownEndTick = 600L;
        source.pyroSpearCooldownEndTick = 700L;
        source.pyroRingCooldownEndTick = 800L;

        PlayerMysteryData copied = new PlayerMysteryData();
        copied.copyFrom(source);

        assertEquals(900L, copied.paperSubstituteArmedEndTick);
        assertEquals("minecraft:overworld", copied.paperSubstituteDimension);
        assertEquals(12.5d, copied.paperSubstituteX);
        assertEquals(70d, copied.paperSubstituteY);
        assertEquals(-4.5d, copied.paperSubstituteZ);
        assertEquals(400L, copied.flameLeapCooldownEndTick);
        assertEquals(4, copied.clownDodgeCount);
        assertEquals(12L, copied.lastRestRecoveryDay);
        assertTrue(copied.m1TrialActive);
        assertEquals(100L, copied.m1TrialStartTick);
        assertEquals(200L, copied.m1TrialElapsedTicks);
        assertTrue(copied.m1TrialCampVisited);
        assertEquals(7, copied.m1TrialBestSequence);
        assertEquals(3, copied.m1TrialOccultKills);
        assertEquals(2, copied.m1TrialActingEvents);
        assertEquals(42f, copied.m1TrialMaxPressure);
        assertEquals(300L, copied.thiefPilferCooldownEndTick);
        assertEquals(600L, copied.thiefEscapeCooldownEndTick);
        assertEquals(200L, copied.apprenticeTrickCooldownEndTick);
        assertEquals(400L, copied.apprenticeCopyCooldownEndTick);
        assertEquals(500L, copied.psychPacifyCooldownEndTick);
        assertEquals(600L, copied.psychShockCooldownEndTick);
        assertEquals(700L, copied.pyroSpearCooldownEndTick);
        assertEquals(800L, copied.pyroRingCooldownEndTick);
    }
}
