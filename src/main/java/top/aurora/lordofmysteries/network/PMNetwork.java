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

    private static final String PROTOCOL_VERSION = "1";

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
    }
}
