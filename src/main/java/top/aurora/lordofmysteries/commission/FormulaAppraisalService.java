package top.aurora.lordofmysteries.commission;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.registry.ModItems;

public final class FormulaAppraisalService {

    private static final String SEED = "lom_formula_seed";
    private static final String AUTHENTIC = "lom_formula_authentic";
    private static final String CLUE_MASK = "lom_formula_clue_mask";
    private static final String APPRAISED = "lom_formula_appraised";
    private static final String VERDICT = "lom_formula_verdict";
    private static final String FAILED_ATTEMPTS = "lom_formula_failed_attempts";
    private static final ResourceLocation AUTHENTICITY_KNOWLEDGE =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "knowledge/m2/formula_authenticity");

    private FormulaAppraisalService() {}

    public static ItemStack createDossier(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        long seed = FormulaAppraisalLogic.dossierSeed(
                player.serverLevel().getSeed(), player.getUUID(),
                data.commissionAcceptedTick);
        boolean authentic = FormulaAppraisalLogic.isAuthentic(seed);
        ItemStack dossier = new ItemStack(ModItems.SEALED_FORMULA_DOSSIER.get());
        dossier.getOrCreateTag().putLong(SEED, seed);
        dossier.getOrCreateTag().putBoolean(AUTHENTIC, authentic);
        dossier.getOrCreateTag().putInt(
                CLUE_MASK, FormulaAppraisalLogic.clueMask(seed, authentic));
        return dossier;
    }

    public static ItemStack createRecoveryDossier(
            ServerPlayer player,
            boolean appraised) {
        ItemStack dossier = createDossier(player);
        if (appraised) {
            dossier.getOrCreateTag().putBoolean(APPRAISED, true);
        }
        return dossier;
    }

    public static int inspect(ServerPlayer player, ItemStack dossier) {
        if (!dossier.is(ModItems.SEALED_FORMULA_DOSSIER.get())) return 0;
        if (!CommissionService.isCurrentObjective(
                player, "custom_callback", "formula_appraised")
                && !dossier.getOrCreateTag().getBoolean(APPRAISED)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula.wrong_stage")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        if (!dossier.getOrCreateTag().getBoolean(APPRAISED)) {
            PlayerMysteryData data = MysteryCapability.get(player);
            boolean creative = player.getAbilities().instabuild;
            FormulaAppraisalLogic.Method method = FormulaAppraisalLogic.selectMethod(
                    SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                            && data.sequence <= 9,
                    creative ? 100f : data.spirituality,
                    creative || hasItem(player, ModItems.DIVINATION_CRYSTAL.get()),
                    creative || hasItem(player, ModItems.MYSTIC_INK.get()));
            if (method == FormulaAppraisalLogic.Method.UNAVAILABLE) {
                player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.formula.missing_method")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            if (!creative && method == FormulaAppraisalLogic.Method.SPIRITUAL) {
                data.spirituality -= 6f;
            } else if (!creative) {
                removeOne(player, ModItems.MYSTIC_INK.get());
            }
            dossier.getOrCreateTag().putBoolean(APPRAISED, true);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula.method."
                            + method.name().toLowerCase())
                    .withStyle(ChatFormatting.GOLD));
            CommissionService.recordObjective(
                    player, "custom_callback", "formula_appraised", 1);
        } else if (CommissionService.isCurrentObjective(
                player, "custom_callback", "formula_appraised")) {
            CommissionService.recordObjective(
                    player, "custom_callback", "formula_appraised", 1);
        }
        showClues(player, dossier);
        return 1;
    }

    public static int inspectHeld(ServerPlayer player) {
        ItemStack dossier = findDossier(player);
        if (dossier.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula.no_dossier")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        return inspect(player, dossier);
    }

    public static int submitVerdict(ServerPlayer player,
                                    boolean authenticVerdict) {
        ItemStack dossier = findDossier(player);
        if (dossier.isEmpty() || !dossier.getOrCreateTag().getBoolean(APPRAISED)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula.not_appraised")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!CommissionService.isCurrentObjective(
                player, "custom_callback", "formula_verdict")) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula.wrong_stage")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        boolean authentic = dossier.getOrCreateTag().getBoolean(AUTHENTIC);
        if (!FormulaAppraisalLogic.verdictMatches(authentic, authenticVerdict)) {
            PlayerMysteryData data = MysteryCapability.get(player);
            data.insanityPressure = Math.min(100f, data.insanityPressure + 8f);
            dossier.getOrCreateTag().putInt(FAILED_ATTEMPTS,
                    dossier.getOrCreateTag().getInt(FAILED_ATTEMPTS) + 1);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.formula.verdict_wrong")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        dossier.getOrCreateTag().putBoolean(VERDICT, authenticVerdict);
        MysteryCapability.get(player).knownKnowledge.add(AUTHENTICITY_KNOWLEDGE);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.formula.verdict_correct",
                Component.translatable(authenticVerdict
                        ? "message.lord_of_mysteries.formula.authentic"
                        : "message.lord_of_mysteries.formula.forged"))
                .withStyle(ChatFormatting.GREEN));
        return CommissionService.recordObjective(
                player, "custom_callback", "formula_verdict", 1) ? 1 : 0;
    }

    public static boolean hasDossier(ServerPlayer player) {
        return !findDossier(player).isEmpty();
    }

    public static DossierEvidence evidence(ServerPlayer player) {
        ItemStack dossier = findDossier(player);
        if (dossier.isEmpty()) return DossierEvidence.NONE;
        CompoundTag tag = dossier.getTag();
        if (tag == null) return new DossierEvidence(true, false, false, 0);
        return new DossierEvidence(
                true,
                tag.getBoolean(APPRAISED),
                tag.contains(VERDICT),
                tag.getInt(CLUE_MASK));
    }

    public static int failedAttempts(ServerPlayer player) {
        ItemStack dossier = findDossier(player);
        CompoundTag tag = dossier.getTag();
        return tag == null ? 0 : Math.max(0, tag.getInt(FAILED_ATTEMPTS));
    }

    public static void takeDossier(ServerPlayer player) {
        ItemStack dossier = findDossier(player);
        if (!dossier.isEmpty() && !player.getAbilities().instabuild) dossier.shrink(1);
    }

    public static boolean isAppraised(ItemStack dossier) {
        return dossier.getOrCreateTag().getBoolean(APPRAISED);
    }

    private static void showClues(ServerPlayer player, ItemStack dossier) {
        int mask = dossier.getOrCreateTag().getInt(CLUE_MASK);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.formula.clues.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        sendClue(player, "watermark", (mask & 0b001) != 0);
        sendClue(player, "ink", (mask & 0b010) != 0);
        sendClue(player, "sequence", (mask & 0b100) != 0);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.formula.clues.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void sendClue(ServerPlayer player, String clue,
                                 boolean consistent) {
        player.sendSystemMessage(Component.literal("• ").append(
                Component.translatable(
                        "message.lord_of_mysteries.formula.clue." + clue + "."
                                + (consistent ? "consistent" : "suspicious")))
                .withStyle(consistent ? ChatFormatting.GRAY : ChatFormatting.RED));
    }

    private static ItemStack findDossier(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SEALED_FORMULA_DOSSIER.get())) return stack;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(ModItems.SEALED_FORMULA_DOSSIER.get())) return stack;
        }
        for (int slot = 0; slot < player.getEnderChestInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getEnderChestInventory().getItem(slot);
            if (stack.is(ModItems.SEALED_FORMULA_DOSSIER.get())) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        return player.getInventory().items.stream().anyMatch(stack -> stack.is(item))
                || player.getInventory().offhand.stream().anyMatch(stack -> stack.is(item));
    }

    private static boolean removeOne(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    public record DossierEvidence(
            boolean present,
            boolean appraised,
            boolean verdictSubmitted,
            int clueMask) {

        public static final DossierEvidence NONE = new DossierEvidence(
                false, false, false, 0);
    }
}
