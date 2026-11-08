package top.aurora.lordofmysteries.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.characteristic.CharacteristicSeparatorBlock;
import top.aurora.lordofmysteries.characteristic.ImprintWashingAltarBlock;
import top.aurora.lordofmysteries.potion.CrucibleBlock;
import top.aurora.lordofmysteries.ritual.RitualAltarBlock;
import top.aurora.lordofmysteries.ritual.RitualChalkMarkBlock;
import top.aurora.lordofmysteries.hunter.HunterSnareBlock;
import top.aurora.lordofmysteries.commission.CommissionBoardBlock;

/**
 * 方块注册（Forge 1.20.1）。当前包含坩埚、仪式祭坛、阵纹与猎人捕兽夹。
 * 方块实体绑定集中在 {@link ModBlockEntities}。
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

    public static final RegistryObject<Block> SALTSTONE_ORE = BLOCKS.register("saltstone_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(3.0f, 6f).requiresCorrectToolForDrops(),
                    net.minecraft.util.valueproviders.UniformInt.of(1, 3)));
    public static final RegistryObject<Block> DEEPSLATE_SALTSTONE_ORE = BLOCKS.register("deepslate_saltstone_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(4.5f, 6f).requiresCorrectToolForDrops(),
                    net.minecraft.util.valueproviders.UniformInt.of(1, 3)));
    public static final RegistryObject<Block> ARGENT_ORE = BLOCKS.register("argent_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(3.0f, 6f).requiresCorrectToolForDrops(),
                    net.minecraft.util.valueproviders.UniformInt.of(1, 3)));
    public static final RegistryObject<Block> DEEPSLATE_ARGENT_ORE = BLOCKS.register("deepslate_argent_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(4.5f, 6f).requiresCorrectToolForDrops(),
                    net.minecraft.util.valueproviders.UniformInt.of(1, 3)));
    public static final RegistryObject<Block> SPIRIT_HERB_CROP = BLOCKS.register("spirit_herb_crop",
            () -> new top.aurora.lordofmysteries.survival.OccultCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP),
                    () -> ModItems.SPIRIT_HERB.get()));
    public static final RegistryObject<Block> WILD_SPIRIT_HERB = BLOCKS.register("wild_spirit_herb",
            () -> new top.aurora.lordofmysteries.survival.OccultHerbBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS),
                    () -> ModItems.SPIRIT_HERB.get()));
    public static final RegistryObject<Block> HEATHER_CROP = BLOCKS.register("heather_crop",
            () -> new top.aurora.lordofmysteries.survival.OccultCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP),
                    () -> ModItems.HEATHER.get()));
    public static final RegistryObject<Block> WILD_HEATHER = BLOCKS.register("wild_heather",
            () -> new top.aurora.lordofmysteries.survival.OccultHerbBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS),
                    () -> ModItems.HEATHER.get()));
    public static final RegistryObject<Block> MYSTIC_MORTAR = BLOCKS.register("mystic_mortar",
            () -> new top.aurora.lordofmysteries.survival.MysticMortarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(2.5f, 6f).requiresCorrectToolForDrops().noOcclusion()));


    // 仪式祭坛：承载多方块阵列、材料、状态机和结算。
    public static final RegistryObject<Block> RITUAL_ALTAR = BLOCKS.register("ritual_altar",
            () -> new RitualAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    // strength(硬度, 爆炸抗性)。这里接近石质工作台，避免生存模式过脆。
                    .strength(3.0f, 6.0f)
                    // 要求正确工具才掉落，便于后续接入方块标签和镐类工具等级。
                    .requiresCorrectToolForDrops()));

    // 坩埚：CrucibleBlockEntity 的交互载体。
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register("crucible",
            () -> new CrucibleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 6.0f)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> RITUAL_CHALK_MARK =
            BLOCKS.register("ritual_chalk_mark",
                    () -> new RitualChalkMarkBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.QUARTZ)
                            .strength(0.1f)
                            .noCollission()
                            .noOcclusion()));

    public static final RegistryObject<Block> HUNTER_SNARE = BLOCKS.register("hunter_snare",
            () -> new HunterSnareBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.6f)
                    .noOcclusion()));

    public static final RegistryObject<Block> COMMISSION_BOARD = BLOCKS.register(
            "commission_board",
            () -> new CommissionBoardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2.0f)));

    public static final RegistryObject<Block> CHARACTERISTIC_SEPARATOR =
            BLOCKS.register("characteristic_separator",
                    () -> new CharacteristicSeparatorBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .sound(SoundType.METAL)
                                    .strength(4.0f, 8.0f)
                                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> IMPRINT_WASHING_ALTAR =
            BLOCKS.register("imprint_washing_altar",
                    () -> new ImprintWashingAltarBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_PURPLE)
                                    .sound(SoundType.AMETHYST)
                                    .strength(3.5f, 8.0f)
                                    .requiresCorrectToolForDrops()));
}
