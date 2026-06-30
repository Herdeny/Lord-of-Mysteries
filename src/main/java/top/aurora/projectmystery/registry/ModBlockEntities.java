package top.aurora.projectmystery.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 方块实体注册占位（设计文档 §7.2 CrucibleBlockEntity）。
 *
 * M0 阶段仅建立 DeferredRegister，坩埚 BlockEntityType 的真正绑定
 * （含 BlockEntity 实现类、菜单、tick）在 M1「坩埚方块实体 + 基础魔药制作」落地。
 */
public final class ModBlockEntities {

    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE,
                    ProjectMystery.MOD_ID);

    // TODO(M1): public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE = ...
}
