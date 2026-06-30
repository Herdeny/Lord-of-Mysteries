package top.aurora.projectmystery.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 方块注册（Forge 1.20.1）。M0 放仪式祭坛与坩埚的占位方块，验证注册管线。
 * 坩埚的 BlockEntity 绑定在 {@link ModBlockEntities}（M1 落地）。
 */
public final class ModBlocks {

    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectMystery.MOD_ID);

    // 仪式祭坛（§18.5 ritual_altar）
    public static final RegistryObject<Block> RITUAL_ALTAR = BLOCKS.register("ritual_altar",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()));

    // 坩埚（§7.2 CrucibleBlockEntity 的载体）
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register("crucible",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 6.0f)
                    .requiresCorrectToolForDrops()));
}
