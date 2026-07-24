package top.aurora.lordofmysteries.commission;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.world.InvestigationSiteSavedData;
import top.aurora.lordofmysteries.world.MistCityDistrictLayout;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InvestigationNpcHandler {

    public static final String PRESS_CLERK_TAG = "lom_press_clerk";
    public static final String NIGHTHAWK_CONTACT_TAG = "lom_nighthawk_contact";
    public static final String MISSING_REPORTER_TAG = "lom_missing_reporter";
    public static final String OCCULT_APPRAISER_TAG = "lom_occult_appraiser";
    public static final String DETECTIVE_CLERK_TAG = "lom_detective_clerk";
    public static final String CONSTABLE_TAG = "lom_constable";
    public static final String ESCORT_OWNER_TAG = "lom_escort_owner";
    public static final String ESCORT_REPORTER_TAG = "lom_escort_reporter";
    public static final String ESCORT_PARTY_DATA = "lom_escort_party";

    private InvestigationNpcHandler() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof ArmorStand armorStand
                && DynamicCaseManifestationService.isEvidenceDisplay(armorStand)) {
            ItemStack portfolio = event.getEntity().getMainHandItem();
            if (!(portfolio.getItem() instanceof DynamicEvidencePortfolioItem)) {
                portfolio = event.getEntity().getOffhandItem();
            }
            boolean portfolioHeld =
                    portfolio.getItem() instanceof DynamicEvidencePortfolioItem;
            event.setCanceled(true);
            event.setCancellationResult(portfolioHeld
                    ? InteractionResult.sidedSuccess(
                            event.getEntity().level().isClientSide())
                    : InteractionResult.PASS);
            if (event.getHand() == InteractionHand.MAIN_HAND
                    && portfolioHeld
                    && event.getEntity() instanceof ServerPlayer player) {
                DynamicCaseService.collectSceneEvidence(
                        player, armorStand.blockPosition(),
                        portfolio,
                        DynamicCaseManifestationService.evidenceInstanceId(
                                armorStand));
            }
            return;
        }
        if (!(event.getTarget() instanceof Villager villager)
                || !isInvestigationNpc(villager)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(
                event.getEntity().level().isClientSide()));
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (DynamicCaseManifestationService.tryInteract(player, villager)) return;
        if (DynamicCaseService.tryInterviewWitness(player, villager)) return;
        if (villager.getTags().contains(PRESS_CLERK_TAG)) {
            if (DynamicCaseService.tryBriefOrganizationResponse(
                    player,
                    DynamicCaseProfile.Organization.MIST_CITY_PRESS)) {
                return;
            }
            if (CityLifeService.tryWorkPressShift(player)) return;
            CommissionService.talkPressClerk(player);
        } else if (villager.getTags().contains(NIGHTHAWK_CONTACT_TAG)) {
            CommissionService.interactContact(player);
        } else if (villager.getTags().contains(MISSING_REPORTER_TAG)) {
            CommissionService.rescueReporter(player, villager);
        } else if (villager.getTags().contains(OCCULT_APPRAISER_TAG)) {
            CommissionService.interactOccultAppraiser(player);
        } else if (villager.getTags().contains(DETECTIVE_CLERK_TAG)) {
            if (DynamicCaseService.tryBriefOrganizationResponse(
                    player,
                    DynamicCaseProfile.Organization.DETECTIVE_AGENCY)) {
                return;
            }
            CityServiceDeskService.interactDetectiveClerk(player);
        } else if (villager.getTags().contains(CONSTABLE_TAG)) {
            if (DynamicCaseService.tryBriefOrganizationResponse(
                    player,
                    DynamicCaseProfile.Organization.CONSTABULARY)) {
                return;
            }
            CityServiceDeskService.interactConstable(player);
        } else if (villager.getTags().contains(ESCORT_REPORTER_TAG)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.npc.reporter_escorting")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % 100 != 0) return;
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        MistCityOutpostSavedData.get(level).outpost()
                .filter(level::hasChunkAt)
                .ifPresent(position -> ensureOutpostNpcs(level, position));
        InvestigationSiteSavedData data = InvestigationSiteSavedData.get(level);
        data.cultistCamp().filter(level::hasChunkAt)
                .ifPresent(position -> ensureReporter(level, position, data));
        data.occultistHut().filter(level::hasChunkAt)
                .ifPresent(position -> ensureOccultAppraiser(level, position, data));
        DynamicCaseManifestationService.tick(level);
    }

    public static Villager createEscortReporter(
            Villager template, ServerPlayer owner, String partyKey) {
        if (!(template.level() instanceof ServerLevel level)
                || partyKey == null || partyKey.isBlank()) {
            return null;
        }
        Villager reporter = EntityType.VILLAGER.create(level);
        if (reporter == null) return null;
        reporter.setVillagerData(template.getVillagerData());
        reporter.moveTo(template.getX(), template.getY(), template.getZ(),
                template.getYRot(), template.getXRot());
        reporter.setCustomName(template.getCustomName());
        reporter.setCustomNameVisible(true);
        reporter.setNoAi(false);
        reporter.setInvulnerable(true);
        reporter.setPersistenceRequired();
        reporter.addTag(ESCORT_REPORTER_TAG);
        reporter.getPersistentData().putUUID(ESCORT_OWNER_TAG, owner.getUUID());
        reporter.getPersistentData().putString(ESCORT_PARTY_DATA, partyKey);
        if (!level.addFreshEntity(reporter)) return null;
        return reporter;
    }

    public static void finishEscort(Villager reporter, BlockPos outpost) {
        reporter.getNavigation().stop();
        if (reporter.getTags().contains(ESCORT_REPORTER_TAG)) {
            reporter.discard();
        } else {
            reporter.setNoAi(true);
            reporter.moveTo(outpost.getX() + 0.5d, outpost.getY() + 1d,
                    outpost.getZ() + 2.5d,
                    reporter.getYRot(), reporter.getXRot());
        }
    }

    public static boolean escortBelongsTo(
            Villager reporter, String partyKey) {
        return reporter.getTags().contains(ESCORT_REPORTER_TAG)
                && partyKey != null
                && partyKey.equals(reporter.getPersistentData()
                        .getString(ESCORT_PARTY_DATA));
    }

    public static void restoreBaseReporter(
            Villager reporter, BlockPos camp) {
        reporter.getNavigation().stop();
        reporter.setNoAi(true);
        reporter.getPersistentData().remove(ESCORT_OWNER_TAG);
        reporter.moveTo(camp.getX() - 1.5d, camp.getY(),
                camp.getZ() + 0.5d,
                reporter.getYRot(), reporter.getXRot());
    }

    private static boolean isInvestigationNpc(Villager villager) {
        return villager.getTags().contains(PRESS_CLERK_TAG)
                || villager.getTags().contains(NIGHTHAWK_CONTACT_TAG)
                || villager.getTags().contains(MISSING_REPORTER_TAG)
                || villager.getTags().contains(OCCULT_APPRAISER_TAG)
                || villager.getTags().contains(DETECTIVE_CLERK_TAG)
                || villager.getTags().contains(CONSTABLE_TAG)
                || villager.getTags().contains(ESCORT_REPORTER_TAG)
                || DynamicCaseManifestationService.isManifestationNpc(villager);
    }

    private static void ensureOutpostNpcs(ServerLevel level, BlockPos outpost) {
        boolean formalDistricts = MistCityOutpostSavedData.get(level)
                .serviceVersion() >= 2;
        BlockPos pressPosition = formalDistricts
                ? MistCityDistrictLayout.servicePosition(
                        outpost, MistCityDistrictLayout.District.PRESS)
                : outpost.offset(-2, 1, -1);
        BlockPos detectivePosition = formalDistricts
                ? MistCityDistrictLayout.servicePosition(
                        outpost,
                        MistCityDistrictLayout.District.DETECTIVE_AGENCY)
                : outpost.offset(-5, 1, 3);
        BlockPos constablePosition = formalDistricts
                ? MistCityDistrictLayout.servicePosition(
                        outpost, MistCityDistrictLayout.District.CONSTABULARY)
                : outpost.offset(5, 1, 3);
        ensureVillager(level, pressPosition, PRESS_CLERK_TAG,
                "entity.lord_of_mysteries.press_clerk",
                VillagerProfession.LIBRARIAN, villager -> villager.setNoAi(true));
        ensureVillager(level, outpost.offset(2, 1, -1), NIGHTHAWK_CONTACT_TAG,
                "entity.lord_of_mysteries.nighthawk_contact",
                VillagerProfession.CLERIC, villager -> villager.setNoAi(true));
        ensureVillager(level, detectivePosition, DETECTIVE_CLERK_TAG,
                "entity.lord_of_mysteries.detective_clerk",
                VillagerProfession.CARTOGRAPHER, villager -> villager.setNoAi(true));
        ensureVillager(level, constablePosition, CONSTABLE_TAG,
                "entity.lord_of_mysteries.constable",
                VillagerProfession.WEAPONSMITH, villager -> villager.setNoAi(true));
    }

    private static void ensureReporter(ServerLevel level, BlockPos camp,
                                       InvestigationSiteSavedData data) {
        if (data.reporterId().map(level::getEntity)
                .filter(Villager.class::isInstance)
                .map(Villager.class::cast)
                .filter(Villager::isAlive).isPresent()) return;
        Villager reporter = ensureVillager(level, camp.offset(-2, 0, 0),
                MISSING_REPORTER_TAG, "entity.lord_of_mysteries.missing_reporter",
                VillagerProfession.CARTOGRAPHER, villager -> {
                    villager.setNoAi(true);
                    villager.setInvulnerable(true);
                });
        if (reporter != null) data.recordReporter(reporter.getUUID());
    }

    private static void ensureOccultAppraiser(ServerLevel level, BlockPos hut,
                                              InvestigationSiteSavedData data) {
        if (data.occultAppraiserId().map(level::getEntity)
                .filter(Villager.class::isInstance)
                .map(Villager.class::cast)
                .filter(Villager::isAlive).isPresent()) return;
        Villager appraiser = ensureVillager(level, hut.offset(0, 1, -1),
                OCCULT_APPRAISER_TAG, "entity.lord_of_mysteries.occult_appraiser",
                VillagerProfession.CLERIC, villager -> villager.setNoAi(true));
        if (appraiser != null) data.recordOccultAppraiser(appraiser.getUUID());
    }

    private static Villager ensureVillager(ServerLevel level, BlockPos position,
                                           String tag, String nameKey,
                                           VillagerProfession profession,
                                           Consumer<Villager> setup) {
        Villager existing = level.getEntitiesOfClass(
                        Villager.class, new AABB(position).inflate(16d),
                        villager -> villager.getTags().contains(tag))
                .stream().findFirst().orElse(null);
        if (existing != null) {
            if (existing.distanceToSqr(
                    position.getX() + 0.5d,
                    position.getY(),
                    position.getZ() + 0.5d) > 4d) {
                existing.getNavigation().stop();
                existing.moveTo(position.getX() + 0.5d, position.getY(),
                        position.getZ() + 0.5d,
                        existing.getYRot(), existing.getXRot());
            }
            setup.accept(existing);
            return existing;
        }
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) return null;
        villager.setVillagerData(villager.getVillagerData()
                .setType(VillagerType.PLAINS)
                .setProfession(profession)
                .setLevel(2));
        villager.moveTo(position.getX() + 0.5d, position.getY(),
                position.getZ() + 0.5d, 0f, 0f);
        villager.setCustomName(Component.translatable(nameKey));
        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();
        villager.setInvulnerable(true);
        villager.addTag(tag);
        setup.accept(villager);
        level.addFreshEntity(villager);
        ProjectMystery.LOGGER.info("Spawned investigation NPC {} at {}", tag, position);
        return villager;
    }
}
