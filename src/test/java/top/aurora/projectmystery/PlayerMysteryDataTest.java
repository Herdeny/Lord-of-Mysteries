package top.aurora.projectmystery;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import top.aurora.projectmystery.player.PlayerMysteryData;

/**
 * M0 基础单元测试框架（设计文档 §20 M0 任务：基础单元测试框架）。
 * 验证 PlayerMysteryData 默认值与普通人/非凡者判定逻辑。
 *
 * 注：test 源集默认不在 Minecraft 运行时类路径上（NeoForge ModDev），
 * 因此这里只覆盖不依赖 net.minecraft.* 的纯 POJO 逻辑。涉及 ResourceLocation /
 * Codec 反序列化 / 能力检定的测试放到 gameTestServer（§20 M0 任务）。
 */
class PlayerMysteryDataTest {

    @Test
    void defaultsAreCommoner() {
        PlayerMysteryData d = new PlayerMysteryData();
        assertNull(d.pathway, "默认应为普通人（pathway=null）");
        assertEquals(-1, d.sequence, "默认序列应为 -1");
        assertFalse(d.isExtraordinary(), "默认不应为非凡者");
        assertEquals(0f, d.spirituality);
        assertEquals(100f, d.spiritualityMax);
        assertEquals(0f, d.pollution);
        assertEquals(1, d.schemaVersion);
    }

    @Test
    void notExtraordinaryWithoutPathwayEvenIfSequenceSet() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.sequence = 9; // 仅有序列、无途径 → 仍非非凡者
        assertFalse(d.isExtraordinary(), "无途径时不应判定为非凡者");
    }

    @Test
    void sequenceBoundaryStaysCommonerWhenNegative() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.sequence = -1;
        assertFalse(d.isExtraordinary());
    }
}
