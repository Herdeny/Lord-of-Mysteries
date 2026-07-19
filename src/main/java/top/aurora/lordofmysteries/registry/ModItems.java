package top.aurora.lordofmysteries.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.ForgeSpawnEggItem;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.potion.SpectatorPotionItem;
import top.aurora.lordofmysteries.potion.HunterPotionItem;
import top.aurora.lordofmysteries.potion.M2PathwayPotionItem;
import top.aurora.lordofmysteries.artifact.EternalMatchboxItem;
import top.aurora.lordofmysteries.artifact.CalmingIncenseItem;
import top.aurora.lordofmysteries.artifact.SpiritLanternItem;
import top.aurora.lordofmysteries.artifact.ProtectiveCharmItem;
import top.aurora.lordofmysteries.knowledge.InvestigatorCompassItem;
import top.aurora.lordofmysteries.knowledge.InvestigatorNotesItem;
import top.aurora.lordofmysteries.knowledge.FormulaFragmentItem;
import top.aurora.lordofmysteries.knowledge.KnowledgeCopyItem;
import top.aurora.lordofmysteries.commission.CaseClueItem;
import top.aurora.lordofmysteries.commission.DynamicEvidencePortfolioItem;
import top.aurora.lordofmysteries.commission.MistCityNewspaperItem;
import top.aurora.lordofmysteries.commission.CommissionPaperItem;
import top.aurora.lordofmysteries.commission.SealedFormulaDossierItem;
import top.aurora.lordofmysteries.acting.ActingIdentityCardItem;
import top.aurora.lordofmysteries.acting.ActingReflectionJournalItem;
import top.aurora.lordofmysteries.characteristic.BrokenCharacteristicItem;

/**
 * 物品注册（Forge 1.20.1）。包含材料、魔药、封印物和方块物品。
 *
 * <p>本类只负责“声明有哪些物品”。物品行为、右键逻辑、能力触发等应放到对应
 * Item 子类或事件处理器中，避免注册类承担过多业务逻辑。
 */
public final class ModItems {

    /** 注册类只提供静态 RegistryObject，不需要实例。 */
    private ModItems() {}

    /**
     * 本 Mod 的物品注册表。所有注册名都会自动落在 ProjectMystery.MOD_ID 命名空间下，
     * 例如 {@code spirit_herb} 会成为 {@code lord_of_mysteries:spirit_herb}。
     */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ProjectMystery.MOD_ID);

    /**
     * 创建无特殊行为的普通物品。
     *
     * <p>基础材料只需要能注册、显示并被配方或数据 JSON 引用。等某个
     * 物品需要耐久、右键、NBT 或发光效果时，再替换为专门的 Item 子类。
     */
    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    // —— 基础材料（v0.8 长期材料池的当前可玩子集）——
    // 基础灵性材料。后续可作为采集、掉落、交易、魔药制作的共同输入。
    public static final RegistryObject<Item> SPIRIT_HERB = simple("spirit_herb");

    // 占卜和序列 9 魔药用材料。
    public static final RegistryObject<Item> DIVINATION_CRYSTAL = simple("divination_crystal");

    // 月相/夜晚相关材料，占位给魔药品质加成和仪式媒介使用。
    public static final RegistryObject<Item> MOONWATER = simple("moonwater");
    public static final RegistryObject<Item> PURE_WATER = simple("pure_water");
    public static final RegistryObject<Item> WHITE_CANDLE = simple("white_candle");
    public static final RegistryObject<Item> DEEP_GRAY_SPIRIT_TEAR =
            simple("deep_gray_spirit_tear");
    public static final RegistryObject<Item> HEATHER = simple("heather");
    public static final RegistryObject<Item> SPIRIT_ALCOHOL = simple("spirit_alcohol");
    public static final RegistryObject<Item> SHAPESHIFTER_SERPENT_GLAND =
            simple("shapeshifter_serpent_gland");
    public static final RegistryObject<Item> ASH_POWDER = simple("ash_powder");
    public static final RegistryObject<Item> SILVER_FILINGS = simple("silver_filings");
    public static final RegistryObject<Item> SPIRIT_SALT = simple("spirit_salt");
    public static final RegistryObject<Item> ASHEN_THREAD = simple("ashen_thread");
    public static final RegistryObject<Item> SHADOW_MARTEN_CLAW =
            simple("shadow_marten_claw");
    public static final RegistryObject<Item> STARLIGHT_MOSS =
            simple("starlight_moss");
    public static final RegistryObject<Item> DREAM_SCALE_FRAGMENT =
            simple("dream_scale_fragment");
    public static final RegistryObject<Item> EMBER_SALAMANDER_GLAND =
            simple("ember_salamander_gland");
    public static final RegistryObject<Item> MIRROR_CRAB_SHELL =
            simple("mirror_crab_shell");
    public static final RegistryObject<Item> ANCIENT_TABLET_SPORE_SAC =
            simple("ancient_tablet_spore_sac");
    public static final RegistryObject<Item> IRIDESCENT_TRICKBIRD_FEATHER =
            simple("iridescent_trickbird_feather");
    public static final RegistryObject<Item> METEOR_DUST =
            simple("meteor_dust");
    public static final RegistryObject<Item> BLANK_MANUSCRIPT =
            simple("blank_manuscript");
    public static final RegistryObject<Item> MYSTIC_INK = simple("mystic_ink");
    public static final RegistryObject<Item> FORMULA_FRAGMENT = ITEMS.register(
            "formula_fragment",
            () -> new FormulaFragmentItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> NEWSPAPER = ITEMS.register(
            "newspaper",
            () -> new MistCityNewspaperItem(
                    new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> COMMISSION_PAPER = ITEMS.register(
            "commission_paper",
            () -> new CommissionPaperItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DYNAMIC_EVIDENCE_PORTFOLIO = ITEMS.register(
            "dynamic_evidence_portfolio",
            () -> new DynamicEvidencePortfolioItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SEALED_FORMULA_DOSSIER = ITEMS.register(
            "sealed_formula_dossier",
            () -> new SealedFormulaDossierItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BLOODSTAINED_NOTEBOOK = ITEMS.register(
            "bloodstained_notebook",
            () -> new CaseClueItem(
                    "m2/missing_squad_evidence",
                    "tooltip.lord_of_mysteries.bloodstained_notebook",
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BURNT_LIST = ITEMS.register(
            "burnt_list",
            () -> new CaseClueItem(
                    "m2/missing_squad_chain",
                    "tooltip.lord_of_mysteries.burnt_list",
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PRESS_CARD = ITEMS.register(
            "press_card",
            () -> new CaseClueItem(
                    "m2/reporter_rescued",
                    "tooltip.lord_of_mysteries.press_card",
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> KNOWLEDGE_COPY = ITEMS.register(
            "knowledge_copy",
            () -> new KnowledgeCopyItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MYSTIC_PLAYING_CARDS = ITEMS.register(
            "mystic_playing_cards",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PAPER_FIGURINE = ITEMS.register(
            "paper_figurine",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SHAPESHIFTER_SERPENT_SPAWN_EGG = ITEMS.register(
            "shapeshifter_serpent_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.SHAPESHIFTER_SERPENT, 0x32323A, 0x9C6CCF,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_WISP_SPAWN_EGG = ITEMS.register(
            "spirit_wisp_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.SPIRIT_WISP, 0xA6E8FF, 0x5F61A8,
                    new Item.Properties()));
    public static final RegistryObject<Item> ASHEN_PUPPET_SPAWN_EGG = ITEMS.register(
            "ashen_puppet_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.ASHEN_PUPPET, 0x4A4541, 0xB09A82,
                    new Item.Properties()));
    public static final RegistryObject<Item> THIEF_BREAKDOWN_SPAWN_EGG = ITEMS.register(
            "thief_breakdown_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.THIEF_BREAKDOWN, 0x24252A, 0x64748B,
                    new Item.Properties()));
    public static final RegistryObject<Item> APPRENTICE_BREAKDOWN_SPAWN_EGG =
            ITEMS.register("apprentice_breakdown_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.APPRENTICE_BREAKDOWN, 0x171B2E, 0x60A5FA,
                            new Item.Properties()));
    public static final RegistryObject<Item> PSYCHIATRIST_BREAKDOWN_SPAWN_EGG =
            ITEMS.register("psychiatrist_breakdown_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.PSYCHIATRIST_BREAKDOWN,
                            0x3D2548, 0xD084C6, new Item.Properties()));
    public static final RegistryObject<Item> PYROMANIAC_BREAKDOWN_SPAWN_EGG =
            ITEMS.register("pyromaniac_breakdown_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.PYROMANIAC_BREAKDOWN,
                            0x33130A, 0xFF6A00, new Item.Properties()));

    // 制作失败或污染反应的产物，用于验证失败结果和污染系统链路。
    public static final RegistryObject<Item> CONTAMINATED_MIXTURE = simple("contaminated_mixture");
    public static final RegistryObject<Item> BROKEN_CHARACTERISTIC = ITEMS.register(
            "broken_characteristic",
            () -> new BrokenCharacteristicItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ACTING_IDENTITY_CARD = ITEMS.register(
            "acting_identity_card",
            () -> new ActingIdentityCardItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ACTING_REFLECTION_JOURNAL = ITEMS.register(
            "acting_reflection_journal",
            () -> new ActingReflectionJournalItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INVESTIGATOR_NOTES = ITEMS.register(
            "investigator_notes",
            () -> new InvestigatorNotesItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INVESTIGATOR_COMPASS = ITEMS.register(
            "investigator_compass",
            () -> new InvestigatorCompassItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CALMING_INCENSE = ITEMS.register(
            "calming_incense",
            () -> new CalmingIncenseItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SPIRIT_LANTERN = ITEMS.register(
            "spirit_lantern",
            () -> new SpiritLanternItem(
                    new Item.Properties().durability(64).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PROTECTIVE_CHARM = ITEMS.register(
            "protective_charm",
            () -> new ProtectiveCharmItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SEER_POTION_9 = ITEMS.register("seer_potion_9",
            () -> new SeerPotionItem(new Item.Properties(), 9));
    public static final RegistryObject<Item> SEER_POTION_8 = ITEMS.register("seer_potion_8",
            () -> new SeerPotionItem(new Item.Properties(), 8));
    public static final RegistryObject<Item> SEER_POTION_7 = ITEMS.register("seer_potion_7",
            () -> new SeerPotionItem(new Item.Properties(), 7));
    public static final RegistryObject<Item> SPECTATOR_POTION_9 = ITEMS.register(
            "spectator_potion_9",
            () -> new SpectatorPotionItem(new Item.Properties(), 9));
    public static final RegistryObject<Item> SPECTATOR_POTION_8 = ITEMS.register(
            "spectator_potion_8",
            () -> new SpectatorPotionItem(new Item.Properties(), 8));
    public static final RegistryObject<Item> SPECTATOR_POTION_7 = ITEMS.register(
            "spectator_potion_7",
            () -> new SpectatorPotionItem(new Item.Properties(), 7));
    public static final RegistryObject<Item> HUNTER_POTION_9 = ITEMS.register(
            "hunter_potion_9",
            () -> new HunterPotionItem(new Item.Properties(), 9));
    public static final RegistryObject<Item> HUNTER_POTION_8 = ITEMS.register(
            "hunter_potion_8",
            () -> new HunterPotionItem(new Item.Properties(), 8));
    public static final RegistryObject<Item> HUNTER_POTION_7 = ITEMS.register(
            "hunter_potion_7",
            () -> new HunterPotionItem(new Item.Properties(), 7));
    public static final RegistryObject<Item> THIEF_POTION_9 = ITEMS.register(
            "thief_potion_9",
            () -> new M2PathwayPotionItem(
                    new Item.Properties(), M2PathwayPotionItem.Pathway.THIEF, 9));
    public static final RegistryObject<Item> THIEF_POTION_8 = ITEMS.register(
            "thief_potion_8",
            () -> new M2PathwayPotionItem(
                    new Item.Properties(), M2PathwayPotionItem.Pathway.THIEF, 8));
    public static final RegistryObject<Item> THIEF_POTION_7 = ITEMS.register(
            "thief_potion_7",
            () -> new M2PathwayPotionItem(
                    new Item.Properties(), M2PathwayPotionItem.Pathway.THIEF, 7));
    public static final RegistryObject<Item> APPRENTICE_POTION_9 = ITEMS.register(
            "apprentice_potion_9",
            () -> new M2PathwayPotionItem(
                    new Item.Properties(), M2PathwayPotionItem.Pathway.APPRENTICE, 9));
    public static final RegistryObject<Item> APPRENTICE_POTION_8 = ITEMS.register(
            "apprentice_potion_8",
            () -> new M2PathwayPotionItem(
                    new Item.Properties(), M2PathwayPotionItem.Pathway.APPRENTICE, 8));
    public static final RegistryObject<Item> APPRENTICE_POTION_7 = ITEMS.register(
            "apprentice_potion_7",
            () -> new M2PathwayPotionItem(
                    new Item.Properties(), M2PathwayPotionItem.Pathway.APPRENTICE, 7));

    // —— 封印物（当前实现永燃火柴盒）——
    public static final RegistryObject<Item> ETERNAL_MATCHBOX = ITEMS.register("eternal_matchbox",
            () -> new EternalMatchboxItem(new Item.Properties().durability(16).rarity(Rarity.RARE)));

    // —— 方块物品 ——
    // BlockItem 让方块可以以物品形式存在于背包和创造模式标签中。
    // 这里通过 RegistryObject.get() 关联方块，Forge 会在合适阶段解析真实 Block 实例。
    public static final RegistryObject<Item> RITUAL_ALTAR_ITEM = ITEMS.register("ritual_altar",
            () -> new BlockItem(ModBlocks.RITUAL_ALTAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> RITUAL_CHALK_MARK_ITEM =
            ITEMS.register("ritual_chalk_mark",
                    () -> new BlockItem(
                            ModBlocks.RITUAL_CHALK_MARK.get(), new Item.Properties()));

    // 坩埚方块物品；未来打开 GUI、保存配方状态的是方块实体，不是这个 BlockItem。
    public static final RegistryObject<Item> CRUCIBLE_ITEM = ITEMS.register("crucible",
            () -> new BlockItem(ModBlocks.CRUCIBLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> HUNTER_SNARE_ITEM = ITEMS.register(
            "hunter_snare",
            () -> new BlockItem(ModBlocks.HUNTER_SNARE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COMMISSION_BOARD_ITEM = ITEMS.register(
            "commission_board",
            () -> new BlockItem(ModBlocks.COMMISSION_BOARD.get(), new Item.Properties()));
}
