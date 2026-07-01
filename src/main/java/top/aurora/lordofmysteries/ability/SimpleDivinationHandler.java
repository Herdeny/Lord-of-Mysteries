package top.aurora.lordofmysteries.ability;

import java.util.List;
import java.util.Random;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

/**
 * 简易占卜主动能力（批次1，设计文档 §6 / §8）。
 *
 * <p>规则：
 * <ul>
 *   <li>消耗 15 灵性；</li>
 *   <li>60s 冷却；</li>
 *   <li>返回一条包含方向/危险等级/状态的占卜结果，按 clarity 扭曲。</li>
 * </ul>
 *
 * 主入口 {@link #cast(ServerPlayer)}——由网络包/指令调用。
 */
public final class SimpleDivinationHandler {

    private SimpleDivinationHandler() {}

    public static final float COST = 15f;
    public static final long COOLDOWN_TICKS = 1200L; // 60s
    public static final double DANGER_SCAN_RADIUS = 32.0;

    /** 结果记录，供网络包/日志/测试使用。 */
    public record Result(boolean success, String message) {
        public static Result fail(String reason) { return new Result(false, reason); }
    }

    /**
     * 执行一次简易占卜。
     *
     * @return 是否成功以及给玩家的展示信息
     */
    public static Result cast(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.pathway == null) {
            player.sendSystemMessage(Component.literal("§c你尚未进入任何途径"));
            return Result.fail("not_extraordinary");
        }

        long now = player.level().getGameTime();
        if (!AbilityCooldowns.ready(data.divinationCooldownEndTick, now)) {
            long left = AbilityCooldowns.remaining(data.divinationCooldownEndTick, now);
            player.sendSystemMessage(Component.literal("§7占卜冷却中：" + (left / 20L) + "s"));
            return Result.fail("cooldown");
        }

        if (!SpiritualityCost.tryConsume(data, COST)) {
            player.sendSystemMessage(Component.literal("§c灵性不足（需 " + COST + "）"));
            return Result.fail("insufficient_spirit");
        }

        data.divinationCooldownEndTick = AbilityCooldowns.start(now, COOLDOWN_TICKS);

        // —— 服务端算真值 ——
        LivingEntity nearest = findNearestHostile(player);
        double trueYaw = 0.0;
        String dangerText;
        if (nearest != null) {
            double dx = nearest.getX() - player.getX();
            double dz = nearest.getZ() - player.getZ();
            trueYaw = Math.atan2(dx, -dz); // 与 MC yaw 语义一致：北=0，顺时针
            double dist = Math.sqrt(dx * dx + dz * dz);
            dangerText = String.format(java.util.Locale.ROOT, "%s 位于 %.1f 格外",
                    nearest.getType().getDescription().getString(), dist);
        } else {
            dangerText = "周围没有明显敌意";
        }

        // —— 计算可信度 ——
        Random rng = new Random(player.getUUID().getLeastSignificantBits() ^ now);
        double spPct = data.spiritualityMax > 0 ? data.spirituality / data.spiritualityMax : 0;
        double base = DivinationCredibility.baseClear(data.sequence, spPct);
        // 附近高序列干扰：M1 尚未有 npc 序列数据，暂用污染 + 玩家人数占位。
        int highSeqNearby = countHighSeqNearby(player);
        double interf = DivinationCredibility.interference(highSeqNearby, data.pollution, 0.0);
        double score = DivinationCredibility.finalScore(base, interf, rng);
        DivinationCredibility.Clarity clarity = DivinationCredibility.classify(score);

        // —— 客户端展示：按 clarity 扭曲 ——
        double shownYaw = DivinationCredibility.distortDirection(trueYaw, clarity, rng);
        String shownDanger = DivinationCredibility.distortText(dangerText, clarity, rng);
        String direction = DivinationCredibility.yawToCardinal8(shownYaw);

        ChatFormatting color = switch (clarity) {
            case CLEAR -> ChatFormatting.AQUA;
            case BLURRED -> ChatFormatting.YELLOW;
            case WRONG -> ChatFormatting.DARK_PURPLE;
        };
        String msg = String.format("§7[%s占卜] §r方向：%s | %s",
                clarity.displayZh(), direction, shownDanger);
        player.sendSystemMessage(Component.literal(msg).withStyle(color));
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.2f);

        return new Result(true, msg);
    }

    private static LivingEntity findNearestHostile(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(DANGER_SCAN_RADIUS);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (!(e instanceof Enemy)) continue;
            double d = e.distanceToSqr(player);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    private static int countHighSeqNearby(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(DANGER_SCAN_RADIUS);
        int c = 0;
        for (Player p : player.level().getEntitiesOfClass(Player.class, box)) {
            if (p == player) continue;
            PlayerMysteryData d = MysteryCapability.get(p);
            if (d.pathway != null && d.sequence >= 0 && d.sequence <= 6) c++;
        }
        return c;
    }

    /** 供 M1 后续把结果字符串拼装到聊天/HUD。 */
    public static List<String> demoLabels() {
        return List.of("清晰", "模糊", "错误");
    }
}
