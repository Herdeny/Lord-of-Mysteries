package top.aurora.lordofmysteries.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.potion.CrucibleBlock;
import top.aurora.lordofmysteries.ritual.RitualAltarBlock;
import top.aurora.lordofmysteries.hunter.HunterSnareBlock;

/**
 * 方块注册（Forge 1.20.1）。M0 放仪式祭坛与坩埚的占位方块，验证注册管线。
 * 坩埚的 BlockEntity 绑定在 {@link ModBlockEntities}（M1 落地）。
 *
 * <p>本类只注册基础 Block。需要保存状态、每 tick 工作、打开菜单的方块，应在后续
 * 阶段拆出 Block 子类和 BlockEntity，而不是把复杂逻辑塞进这里。
 */
public final class ModBlocks {

    /** 注册类不承载实例状态。 */
    private ModBlocks() {}

    /**
     * 本 Mod 的方块注册表。注册项路径与 blockstates/models/lang 中的资源名保持一致。
     */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectMystery.MOD_ID);

    // 仪式祭坛（§18.5 ritual_altar）。
    // M0 使用原版 Block 验证注册、模型和掉落工具要求；M1/M2 可替换为 RitualAltarBlock。
    public static final RegistryObject<Block> RITUAL_ALTAR = BLOCKS.register("ritual_altar",
            () -> new RitualAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    // strength(硬度, 爆炸抗性)。这里接近石质工作台，避免生存模式过脆。
                    .strength(3.0f, 6.0f)
                    // 要求正确工具才掉落，便于后续接入方块标签和镐类工具等级。
                    .requiresCorrectToolForDrops()));

    // 坩埚（§7.2 CrucibleBlockEntity 的载体）。
    // 目前只是普通方块；魔药制作状态、材料槽和 tick 逻辑会转移到 CrucibleBlockEntity。
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register("crucible",
            () -> new CrucibleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 6.0f)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> HUNTER_SNARE = BLOCKS.register("hunter_snare",
            () -> new HunterSnareBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.6f)
                    .noOcclusion()));
}
