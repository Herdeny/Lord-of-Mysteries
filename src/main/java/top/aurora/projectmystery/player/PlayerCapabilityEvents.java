package top.aurora.projectmystery.player;

import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterCapabilitiesEvent;

import top.aurora.projectmystery.ProjectMystery;

/**
 * Capability 生命周期接线（Forge 1.20.1）。
 *
 * - RegisterCapabilitiesEvent（mod 总线）：注册 PlayerMysteryData 类型
 * - AttachCapabilitiesEvent（game 总线）：给每个 Player 附着 Provider
 * - PlayerEvent.Clone（game 总线）：死亡/跨维度时拷贝数据（§5 默认保留身份）
 */
public final class PlayerCapabilityEvents {

    private PlayerCapabilityEvents() {}

    /** mod 事件总线监听 */
    @Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            MysteryCapability.register(event);
        }
    }

    /** game 事件总线监听 */
    @Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {

        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(MysteryCapability.ID, new MysteryCapability.Provider());
            }
        }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            Player oldP = event.getOriginal();
            Player newP = event.getEntity();
            // revive 以读取已失效实体上的 capability
            oldP.reviveCaps();
            oldP.getCapability(MysteryCapability.MYSTERY_DATA).ifPresent(oldData ->
                newP.getCapability(MysteryCapability.MYSTERY_DATA).ifPresent(newData ->
                    newData.copyFrom(oldData)));
            oldP.invalidateCaps();
        }
    }
}
