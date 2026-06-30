package top.aurora.projectmystery.registry;

import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 方块注册。M0 先放一个仪式祭坛与坩埚的占位方块，验证注册管线。
 * 实体方块（坩埚）的 BlockEntity 绑定在 {@link ModBlockEntities}。
 */
public final class ModBlocks {

    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ProjectMystery.MOD_ID);

    // 仪式祭坛（§18.5 ritual_altar）—— M0 占位为普通石质方块
    public static final DeferredBlock<Block> RITUAL_ALTAR =
            BLOCKS.registerSimpleBlock("ritual_altar",
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.0f, 6.0f)
                            .requiresCorrectToolForDrops());

    // 坩埚（§7.2 CrucibleBlockEntity 的载体）—— M0 占位，BlockEntity 在 M1 绑定
    public static final DeferredBlock<Block> CRUCIBLE =
            BLOCKS.registerSimpleBlock("crucible",
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.5f, 6.0f)
                            .requiresCorrectToolForDrops());
}
