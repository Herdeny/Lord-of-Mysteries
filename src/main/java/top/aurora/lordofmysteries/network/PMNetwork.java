package top.aurora.lordofmysteries.network;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 网络包总线（Forge 1.20.1 SimpleChannel）。
 *
 * <p>批次1 只涉及两条 C2S 控制包：
 * <ul>
 *   <li>{@link ToggleSpiritVisionC2SPacket}：客户端按键切灵视开关；</li>
 *   <li>{@link UseSimpleDivinationC2SPacket}：客户端按键触发简易占卜。</li>
 * </ul>
 * S2C 反馈仍由 {@code ServerPlayer.sendSystemMessage} 走原版聊天通道，
 * 节省一次 codec 编写，直到需要独立 UI 时再拆出专用包。
 */
public final class PMNetwork {

    private PMNetwork() {}

    private static final String PROTOCOL_VERSION = "4";

    /** SimpleChannel 实例；ChannelId 稳定，客户端/服务端版本必须一致。 */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    /** 在 commonSetup 中调用一次，按顺序注册包。顺序即包 discriminator，切勿改。 */
    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(ToggleSpiritVisionC2SPacket.class, id++)
                .encoder(ToggleSpiritVisionC2SPacket::encode)
                .decoder(ToggleSpiritVisionC2SPacket::decode)
                .consumerMainThread(ToggleSpiritVisionC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseSimpleDivinationC2SPacket.class, id++)
                .encoder(UseSimpleDivinationC2SPacket::encode)
                .decoder(UseSimpleDivinationC2SPacket::decode)
                .consumerMainThread(UseSimpleDivinationC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(RequestMysteryStatusC2SPacket.class, id++)
                .encoder(RequestMysteryStatusC2SPacket::encode)
                .decoder(RequestMysteryStatusC2SPacket::decode)
                .consumerMainThread(RequestMysteryStatusC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(PlayerMysteryStatusS2CPacket.class, id++)
                .encoder(PlayerMysteryStatusS2CPacket::encode)
                .decoder(PlayerMysteryStatusS2CPacket::decode)
                .consumerMainThread(PlayerMysteryStatusS2CPacket::handle)
                .add();
        CHANNEL.messageBuilder(ToggleEmotionReadC2SPacket.class, id++)
                .encoder(ToggleEmotionReadC2SPacket::encode)
                .decoder(ToggleEmotionReadC2SPacket::decode)
                .consumerMainThread(ToggleEmotionReadC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseSurfaceReadC2SPacket.class, id++)
                .encoder(UseSurfaceReadC2SPacket::encode)
                .decoder(UseSurfaceReadC2SPacket::decode)
                .consumerMainThread(UseSurfaceReadC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseMentalSuggestionC2SPacket.class, id++)
                .encoder(UseMentalSuggestionC2SPacket::encode)
                .decoder(UseMentalSuggestionC2SPacket::decode)
                .consumerMainThread(UseMentalSuggestionC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseProvokeC2SPacket.class, id++)
                .encoder(UseProvokeC2SPacket::encode)
                .decoder(UseProvokeC2SPacket::decode)
                .consumerMainThread(UseProvokeC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseEnrageC2SPacket.class, id++)
                .encoder(UseEnrageC2SPacket::encode)
                .decoder(UseEnrageC2SPacket::decode)
                .consumerMainThread(UseEnrageC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(UseSeerAbilityC2SPacket.class, id++)
                .encoder(UseSeerAbilityC2SPacket::encode)
                .decoder(UseSeerAbilityC2SPacket::decode)
                .consumerMainThread(UseSeerAbilityC2SPacket::handle)
                .add();
    }
}
