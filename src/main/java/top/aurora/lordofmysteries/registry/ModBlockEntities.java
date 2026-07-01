package top.aurora.lordofmysteries.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 方块实体注册占位（Forge 1.20.1，设计文档 §7.2 CrucibleBlockEntity）。
 *
 * M0 仅建立 DeferredRegister；坩埚 BlockEntityType 的真正绑定
 * （含 BlockEntity 实现类、菜单、tick）在 M1「坩埚方块实体 + 基础魔药制作」落地。
 *
 * <p>BlockEntityType 是“哪些方块可以挂载哪个方块实体”的注册项。只要坩埚需要保存
 * 配料、火候、污染程度或制作进度，就应该通过 BlockEntity 持久化，而不是把数据放在
 * Block 单例里。
 */
public final class ModBlockEntities {

    /** 注册类不需要实例。 */
    private ModBlockEntities() {}

    /** 方块实体类型注册表。M0 暂无实际条目，但先接入注册管线方便后续扩展。 */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ProjectMystery.MOD_ID);

    // 示例结构保留在这里，后续新增 CrucibleBlockEntity 时可以直接沿用：
    // 1. 新建 CrucibleBlockEntity 类；
    // 2. 把 ModBlocks.CRUCIBLE 绑定到 BlockEntityType；
    // 3. 在对应 Block 子类中实现 EntityBlock#createBlockEntity。
    // TODO(M1): CRUCIBLE = BLOCK_ENTITIES.register("crucible", () ->
    //     BlockEntityType.Builder.of(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get()).build(null));
}
