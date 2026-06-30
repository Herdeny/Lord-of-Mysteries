package top.aurora.projectmystery.player;

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

import top.aurora.projectmystery.ProjectMystery;

/**
 * 玩家非凡者数据的 Forge Capability（设计文档 §5.1，1.20.1 实现）。
 *
 * 取代 NeoForge 的 Attachments：通过 AttachCapabilitiesEvent 把
 * {@link PlayerMysteryData} 附着到 Player，随玩家 NBT 存档。
 * 事件接线见 {@link PlayerCapabilityEvents}。
 */
public final class MysteryCapability {

    private MysteryCapability() {}

    public static final Capability<PlayerMysteryData> MYSTERY_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "mystery_data");

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerMysteryData.class);
    }

    /** 便捷获取：拿不到时返回一个临时空数据，避免 NPE（只读场景）。 */
    public static PlayerMysteryData get(Player player) {
        return player.getCapability(MYSTERY_DATA).orElseGet(PlayerMysteryData::new);
    }

    /** Provider：把 PlayerMysteryData 包装成可序列化 Capability。 */
    public static final class Provider implements ICapabilitySerializable<CompoundTag> {

        private final PlayerMysteryData data = new PlayerMysteryData();
        private final LazyOptional<PlayerMysteryData> optional = LazyOptional.of(dataSupplier());

        private NonNullSupplier<PlayerMysteryData> dataSupplier() {
            return () -> data;
        }

        public PlayerMysteryData getData() {
            return data;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == MYSTERY_DATA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.save();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.load(nbt);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }

    // 引用 Tag 以保证类被加载（部分映射环境下的保险）
    @SuppressWarnings("unused")
    private static final int TAG_GUARD = Tag.TAG_COMPOUND;
}
