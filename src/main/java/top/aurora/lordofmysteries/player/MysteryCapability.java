package top.aurora.lordofmysteries.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 玩家非凡者数据的 Forge Capability（设计文档 §5.1，1.20.1 实现）。
 *
 * 取代 NeoForge 的 Attachments：通过 AttachCapabilitiesEvent 把
 * {@link PlayerMysteryData} 附着到 Player，随玩家 NBT 存档。
 * 事件接线见 {@link PlayerCapabilityEvents}。
 *
 * <p>Capability 可以理解为“给原版对象外挂一份自定义数据/能力”。这里我们给每个
 * Player 挂一份 PlayerMysteryData，用来保存途径、序列、灵性、污染等跨存档数据。
 */
public final class MysteryCapability {

    /** Capability 工具类不需要实例。 */
    private MysteryCapability() {}

    /**
     * Capability 句柄。Forge 通过 CapabilityToken 在运行时把 PlayerMysteryData
     * 类型映射到一个可查询的能力对象。
     */
    public static final Capability<PlayerMysteryData> MYSTERY_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * 附着到玩家实体上的 capability ID。
     *
     * <p>它会作为实体 NBT 下 capability provider 的标识，因此必须稳定。改名会导致旧
     * 存档找不到原来的玩家数据。
     */
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "mystery_data");

    /**
     * 在 Forge 的 RegisterCapabilitiesEvent 中声明 PlayerMysteryData 可作为 Capability。
     */
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerMysteryData.class);
    }

    /**
     * 便捷获取：拿不到时返回一个临时空数据，避免 NPE。
     *
     * <p>注意：返回的临时对象没有挂回玩家身上，适合只读检查。需要修改并持久化时，
     * 更推荐直接使用 player.getCapability(...).ifPresent(data -> ...)。
     */
    public static PlayerMysteryData get(Player player) {
        return player.getCapability(MYSTERY_DATA).orElseGet(PlayerMysteryData::new);
    }

    /**
     * Provider：把 PlayerMysteryData 包装成可序列化 Capability。
     *
     * <p>Forge 查询能力时面对的是 ICapabilityProvider；Provider 持有真正的数据对象，
     * 并负责把它暴露给外部查询、写入 NBT、从 NBT 恢复。
     */
    public static final class Provider implements ICapabilitySerializable<CompoundTag> {

        /** 该玩家实体上的真实数据实例。 */
        private final PlayerMysteryData data = new PlayerMysteryData();

        /**
         * LazyOptional 是 Forge Capability 的生命周期包装。
         * 当实体失效或 provider 被移除时，必须 invalidate，避免外部缓存继续引用旧数据。
         */
        private final LazyOptional<PlayerMysteryData> optional = LazyOptional.of(dataSupplier());

        /** 单独抽出 supplier，便于 LazyOptional 延迟取到 data。 */
        private NonNullSupplier<PlayerMysteryData> dataSupplier() {
            return () -> data;
        }

        /** 测试或内部事件需要直接访问 provider 持有的数据时使用。 */
        public PlayerMysteryData getData() {
            return data;
        }

        /**
         * Forge 查询 Capability 的入口。
         *
         * @param cap 调用方想要的 Capability
         * @param side 方位参数，实体能力通常不关心方向，因此可以为 null
         */
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            // 只响应本 Mod 的 MYSTERY_DATA；其他能力交给空 Optional 表示“不提供”。
            return cap == MYSTERY_DATA ? optional.cast() : LazyOptional.empty();
        }

        /** 保存到玩家实体 NBT。 */
        @Override
        public CompoundTag serializeNBT() {
            return data.save();
        }

        /** 从玩家实体 NBT 恢复。 */
        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.load(nbt);
        }

        /** 玩家实体失效时释放 LazyOptional，避免 Capability 缓存悬挂。 */
        public void invalidate() {
            optional.invalidate();
        }
    }

    // 引用 Tag 以保证类被加载（部分映射环境下的保险）。该字段不参与业务逻辑。
    @SuppressWarnings("unused")
    private static final int TAG_GUARD = Tag.TAG_COMPOUND;
}
