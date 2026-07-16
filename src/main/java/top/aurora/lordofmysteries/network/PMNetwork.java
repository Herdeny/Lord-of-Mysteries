package top.aurora.lordofmysteries.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 网络包总线（Forge 1.20.1 SimpleChannel）。
 *
 * <p>协议版本和固定 discriminator 由 {@link NetworkProtocol} 统一维护。
 * 所有客户端意图进入处理器前都经过服务端 tick 频率门禁；状态面板使用专用 S2C 包，
 * 其余短反馈继续复用原版聊天通道。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PMNetwork {

    private PMNetwork() {}

    private static final Map<UUID, Map<Integer, Long>> LAST_C2S_TICKS =
            new ConcurrentHashMap<>();

    /** SimpleChannel 实例；ChannelId 稳定，客户端/服务端版本必须一致。 */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "main"),
            () -> NetworkProtocol.VERSION,
            NetworkProtocol::accepts,
            NetworkProtocol::accepts);

    /** 在 commonSetup 中调用一次；既有 discriminator 只能保留或追加，禁止重排。 */
    public static void register() {
        CHANNEL.messageBuilder(ToggleSpiritVisionC2SPacket.class,
                        NetworkProtocol.TOGGLE_SPIRIT_VISION)
                .encoder(ToggleSpiritVisionC2SPacket::encode)
                .decoder(ToggleSpiritVisionC2SPacket::decode)
                .consumerMainThread(ToggleSpiritVisionC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseSimpleDivinationC2SPacket.class,
                        NetworkProtocol.USE_SIMPLE_DIVINATION)
                .encoder(UseSimpleDivinationC2SPacket::encode)
                .decoder(UseSimpleDivinationC2SPacket::decode)
                .consumerMainThread(UseSimpleDivinationC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(RequestMysteryStatusC2SPacket.class,
                        NetworkProtocol.REQUEST_STATUS)
                .encoder(RequestMysteryStatusC2SPacket::encode)
                .decoder(RequestMysteryStatusC2SPacket::decode)
                .consumerMainThread(RequestMysteryStatusC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(PlayerMysteryStatusS2CPacket.class,
                        NetworkProtocol.PLAYER_STATUS)
                .encoder(PlayerMysteryStatusS2CPacket::encode)
                .decoder(PlayerMysteryStatusS2CPacket::decode)
                .consumerMainThread(PlayerMysteryStatusS2CPacket::handle)
                .add();
        CHANNEL.messageBuilder(ToggleEmotionReadC2SPacket.class,
                        NetworkProtocol.TOGGLE_EMOTION_READ)
                .encoder(ToggleEmotionReadC2SPacket::encode)
                .decoder(ToggleEmotionReadC2SPacket::decode)
                .consumerMainThread(ToggleEmotionReadC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseSurfaceReadC2SPacket.class,
                        NetworkProtocol.USE_SURFACE_READ)
                .encoder(UseSurfaceReadC2SPacket::encode)
                .decoder(UseSurfaceReadC2SPacket::decode)
                .consumerMainThread(UseSurfaceReadC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseMentalSuggestionC2SPacket.class,
                        NetworkProtocol.USE_MENTAL_SUGGESTION)
                .encoder(UseMentalSuggestionC2SPacket::encode)
                .decoder(UseMentalSuggestionC2SPacket::decode)
                .consumerMainThread(UseMentalSuggestionC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseProvokeC2SPacket.class,
                        NetworkProtocol.USE_PROVOKE)
                .encoder(UseProvokeC2SPacket::encode)
                .decoder(UseProvokeC2SPacket::decode)
                .consumerMainThread(UseProvokeC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseEnrageC2SPacket.class,
                        NetworkProtocol.USE_ENRAGE)
                .encoder(UseEnrageC2SPacket::encode)
                .decoder(UseEnrageC2SPacket::decode)
                .consumerMainThread(UseEnrageC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseSeerAbilityC2SPacket.class,
                        NetworkProtocol.USE_SEER_ABILITY)
                .encoder(UseSeerAbilityC2SPacket::encode)
                .decoder(UseSeerAbilityC2SPacket::decode)
                .consumerMainThread(UseSeerAbilityC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseM2FoundationAbilityC2SPacket.class,
                        NetworkProtocol.USE_M2_FOUNDATION_ABILITY)
                .encoder(UseM2FoundationAbilityC2SPacket::encode)
                .decoder(UseM2FoundationAbilityC2SPacket::decode)
                .consumerMainThread(UseM2FoundationAbilityC2SPacket::handle)
                .add();
    }

    public static boolean acceptC2S(ServerPlayer player, int packetId) {
        return acceptC2S(player, packetId, 2L);
    }

    public static boolean acceptC2S(ServerPlayer player, int packetId,
                                    long minimumIntervalTicks) {
        Map<Integer, Long> playerTicks = LAST_C2S_TICKS.computeIfAbsent(
                player.getUUID(), ignored -> new ConcurrentHashMap<>());
        return C2SRequestGate.allow(playerTicks, packetId,
                player.level().getGameTime(), minimumIntervalTicks);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_C2S_TICKS.remove(event.getEntity().getUUID());
    }
}
