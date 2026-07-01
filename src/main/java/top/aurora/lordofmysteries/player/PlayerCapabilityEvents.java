package top.aurora.lordofmysteries.player;

import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * Capability 生命周期接线（Forge 1.20.1）。
 *
 * - RegisterCapabilitiesEvent（mod 总线）：注册 PlayerMysteryData 类型
 * - AttachCapabilitiesEvent（game 总线）：给每个 Player 附着 Provider
 * - PlayerEvent.Clone（game 总线）：死亡/跨维度时拷贝数据（§5 默认保留身份）
 *
 * <p>这里拆成 ModBus 和 ForgeBus 两个内部类，是因为 Forge 事件分属不同总线。
 * 注册类型属于加载期事件，附着/复制属于游戏期事件，不能混在同一个 bus 上监听。
 */
public final class PlayerCapabilityEvents {

    /** 事件订阅类只包含静态监听方法，不需要实例。 */
    private PlayerCapabilityEvents() {}

    /**
     * mod 事件总线监听。
     *
     * <p>该总线只处理加载期事件。RegisterCapabilitiesEvent 必须在这里监听，否则 Forge
     * 不知道 PlayerMysteryData 可以作为 capability 类型使用。
     */
    @Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {

        /** 向 Forge 声明本 Mod 的玩家数据 Capability 类型。 */
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            MysteryCapability.register(event);
        }
    }

    /**
     * game 事件总线监听。
     *
     * <p>该总线处理实体、玩家、世界 tick 等运行期事件。玩家实例创建后，Capability
     * 也在这里附着。
     */
    @Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {

        /**
         * 给每个 Player 实体附着一份 MysteryCapability.Provider。
         *
         * <p>事件泛型是 Entity，因此需要先判断对象是否为 Player。后续如果要给生物、
         * 方块实体或世界存储非凡数据，可以在对应对象类型上另开 provider。
         */
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(MysteryCapability.ID, new MysteryCapability.Provider());
            }
        }

        /**
         * 玩家实体复制时迁移 Capability 数据。
         *
         * <p>死亡重生、从末地返回主世界等流程会创建新 Player。旧 Player 的 capability
         * 此时可能已经失效，所以读取前需要临时 reviveCaps，拷贝后再 invalidateCaps。
         */
        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            Player oldP = event.getOriginal();
            Player newP = event.getEntity();
            // revive 以读取已失效实体上的 capability；这是 Forge clone 事件的常见写法。
            oldP.reviveCaps();
            oldP.getCapability(MysteryCapability.MYSTERY_DATA).ifPresent(oldData ->
                newP.getCapability(MysteryCapability.MYSTERY_DATA).ifPresent(newData ->
                    newData.copyFrom(oldData)));
            oldP.invalidateCaps();
        }
    }
}
