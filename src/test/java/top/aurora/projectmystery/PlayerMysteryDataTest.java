package top.aurora.projectmystery;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import top.aurora.projectmystery.player.PlayerMysteryData;

/**
 * M0 基础单元测试框架（设计文档 §20 M0 任务：基础单元测试框架）。
 * 验证 PlayerMysteryData 默认值与普通人/非凡者判定逻辑。
 * 注：涉及 Minecraft 运行时类的测试（Codec 反序列化、能力检定）需 gameTestServer，
 * 此处仅覆盖纯 POJO 逻辑。
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
    void becomesExtraordinaryWhenPathwayAndSequenceSet() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.pathway = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("project_mystery", "seer");
        d.sequence = 9;
        assertTrue(d.isExtraordinary());
    }
}
