package top.aurora.projectmystery.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 方块实体注册占位（Forge 1.20.1，设计文档 §7.2 CrucibleBlockEntity）。
 *
 * M0 仅建立 DeferredRegister；坩埚 BlockEntityType 的真正绑定
 * （含 BlockEntity 实现类、菜单、tick）在 M1「坩埚方块实体 + 基础魔药制作」落地。
 */
public final class ModBlockEntities {

    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ProjectMystery.MOD_ID);

    // TODO(M1): CRUCIBLE = BLOCK_ENTITIES.register("crucible", () ->
    //     BlockEntityType.Builder.of(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get()).build(null));
}
