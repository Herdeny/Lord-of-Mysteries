package top.aurora.lordofmysteries.ability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

/**
 * 灵视阵营着色（批次1，设计文档 §6 灵视）。
 *
 * <p>颜色 → 分类：
 * <ul>
 *   <li>GREEN 友好：驯服动物、村民、玩家非战斗中；</li>
 *   <li>YELLOW 中立：动物、无侵略性生物；</li>
 *   <li>RED 敌意：Monster/Enemy 家族；</li>
 *   <li>PURPLE 污染：污染 ≥ 50 的非凡者玩家；</li>
 *   <li>WHITE 高危：污染 ≥ 75 的非凡者玩家；</li>
 *   <li>GRAY 灵体：未来的 spirit/gray-fog 实体（M1 用 tag 判断）。</li>
 * </ul>
 *
 * 分类逻辑放服务端计算并同步到客户端，避免客户端伪造。
 */
public enum SpiritFactionColor {
    GREEN(0x33FF66),
    YELLOW(0xFFEE55),
    RED(0xFF4444),
    PURPLE(0xAA33CC),
    WHITE(0xFFFFFF),
    GRAY(0x888888);

    /** 0xRRGGBB 颜色。用于粒子 / 描边渲染。 */
    public final int rgb;

    SpiritFactionColor(int rgb) {
        this.rgb = rgb;
    }

    /**
     * 依据实体状态判断颜色。
     *
     * <p>服务端调用，结果再打包发给客户端渲染。判断顺序：污染/白危 → 敌意 → 友好 → 中立 → 灵体。
     */
    public static SpiritFactionColor classify(Entity entity) {
        if (entity == null) return GRAY;

        if (entity instanceof Player p) {
            PlayerMysteryData d = MysteryCapability.get(p);
            if (d.pathway != null) {
                if (d.pollution >= 75f) return WHITE;
                if (d.pollution >= 50f) return PURPLE;
            }
            return GREEN;
        }

        if (entity instanceof Enemy) return RED;

        if (entity instanceof AbstractVillager) return GREEN;

        if (entity instanceof LivingEntity le && isTamed(le)) return GREEN;

        if (entity instanceof Animal || entity instanceof WaterAnimal) return YELLOW;

        // 灵体实体在 MVP 扩展，灰雾实体按 v0.6 延至 M4；此处统一兜底为 GRAY。
        if (entity.getType().getDescriptionId().contains("spirit")) return GRAY;

        return entity instanceof Mob ? YELLOW : GRAY;
    }

    private static boolean isTamed(LivingEntity le) {
        // 1.20.1 里 TamableAnimal 位于 net.minecraft.world.entity.TamableAnimal
        try {
            return le instanceof net.minecraft.world.entity.TamableAnimal ta && ta.isTame();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
