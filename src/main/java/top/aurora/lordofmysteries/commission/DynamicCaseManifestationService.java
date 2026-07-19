package top.aurora.lordofmysteries.commission;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.world.InvestigationSiteSavedData;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

final class DynamicCaseManifestationService {

    static final String SUBJECT_TAG = "lom_dynamic_case_subject";
    static final String AFFECTED_TAG = "lom_dynamic_case_affected";
    private static final String EVIDENCE_TAG = "lom_dynamic_case_evidence";
    private static final String INSTANCE_DATA = "lom_dynamic_case_instance";
    private static final String ROLE_DATA = "lom_dynamic_case_role";
    private static final double SEARCH_RANGE = 18d;

    private DynamicCaseManifestationService() {
    }

    static void tick(ServerLevel level) {
        Map<String, DynamicCaseProfile> activeProfiles = new LinkedHashMap<>();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            PlayerMysteryData data = MysteryCapability.get(player);
            DynamicCaseProfile profile = DynamicCaseService.profileFor(player, data);
            if (profile != null) {
                activeProfiles.putIfAbsent(profile.instanceId(), profile);
            }
        }

        Set<BlockPos> loadedAnchors = new LinkedHashSet<>();
        for (DynamicCaseProfile.CaseLocation location
                : DynamicCaseProfile.CaseLocation.values()) {
            manifestationTarget(level, location)
                    .filter(level::hasChunkAt)
                    .ifPresent(loadedAnchors::add);
        }
        for (DynamicCaseProfile profile : activeProfiles.values()) {
            Optional<BlockPos> target = manifestationTarget(
                    level, profile.location());
            if (target.isEmpty() || !level.hasChunkAt(target.get())) continue;
            BlockPos anchor = target.get();
            loadedAnchors.add(anchor);
            ensureManifestation(level, anchor, profile);
        }
        cleanupInactive(level, loadedAnchors, activeProfiles.keySet());
    }

    static boolean isManifestationNpc(Villager villager) {
        return villager.getTags().contains(SUBJECT_TAG)
                || villager.getTags().contains(AFFECTED_TAG);
    }

    static boolean isEvidenceDisplay(ArmorStand armorStand) {
        return armorStand.getTags().contains(EVIDENCE_TAG);
    }

    static boolean tryInteract(ServerPlayer player, Villager villager) {
        if (!isManifestationNpc(villager)) return false;
        PlayerMysteryData data = MysteryCapability.get(player);
        DynamicCaseProfile profile = DynamicCaseService.profileFor(player, data);
        String instanceId = villager.getPersistentData().getString(INSTANCE_DATA);
        if (profile == null || !profile.instanceId().equals(instanceId)) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.dynamic_case.manifestation.outdated")
                    .withStyle(ChatFormatting.GRAY));
            return true;
        }
        if (villager.getTags().contains(SUBJECT_TAG)) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.dynamic_case.subject",
                            Component.translatable(
                                    profile.subject().translationKey("subject")),
                            Component.translatable(
                                    profile.archetype().translationKey("archetype")))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.dynamic_case.affected",
                            Component.translatable(profile.victimImpact()
                                    .translationKey("victim_impact")))
                    .withStyle(ChatFormatting.YELLOW));
        }
        DynamicCaseService.show(player);
        return true;
    }

    private static void ensureManifestation(
            ServerLevel level, BlockPos anchor, DynamicCaseProfile profile) {
        DynamicCaseManifestationPlan plan =
                DynamicCaseManifestationPlan.forProfile(profile);
        int verticalOffset = verticalOffset(profile.location());
        BlockPos subjectPosition = findOpenPosition(level,
                anchor.offset(plan.subject().x(), verticalOffset,
                        plan.subject().z()), Set.of());
        BlockPos affectedPosition = findOpenPosition(level,
                anchor.offset(plan.affected().x(), verticalOffset,
                        plan.affected().z()), Set.of(subjectPosition));
        BlockPos evidencePosition = findOpenPosition(level,
                anchor.offset(plan.evidence().x(), verticalOffset,
                        plan.evidence().z()),
                Set.of(subjectPosition, affectedPosition));
        ensureVillager(level, subjectPosition,
                SUBJECT_TAG, profile, subjectName(profile),
                subjectProfession(profile.subject()), "subject");
        ensureVillager(level, affectedPosition,
                AFFECTED_TAG, profile, affectedName(profile.victimImpact()),
                affectedProfession(profile.victimImpact()), "affected");
        ensureEvidenceDisplay(level, evidencePosition, profile);
    }

    private static void ensureVillager(
            ServerLevel level,
            BlockPos position,
            String tag,
            DynamicCaseProfile profile,
            Component name,
            VillagerProfession profession,
            String role) {
        Villager existing = level.getEntitiesOfClass(
                        Villager.class, new AABB(position).inflate(SEARCH_RANGE),
                        villager -> villager.isAlive()
                                && villager.getTags().contains(tag)
                                && profile.instanceId().equals(villager
                                        .getPersistentData()
                                        .getString(INSTANCE_DATA)))
                .stream().findFirst().orElse(null);
        if (existing != null) return;
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) return;
        villager.setVillagerData(villager.getVillagerData()
                .setType(VillagerType.PLAINS)
                .setProfession(profession)
                .setLevel(2));
        villager.moveTo(position.getX() + 0.5d, position.getY(),
                position.getZ() + 0.5d, 0f, 0f);
        villager.setCustomName(name);
        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();
        villager.setInvulnerable(true);
        villager.setNoAi(true);
        villager.addTag(tag);
        villager.getPersistentData().putString(
                INSTANCE_DATA, profile.instanceId());
        villager.getPersistentData().putString(ROLE_DATA, role);
        level.addFreshEntity(villager);
        ProjectMystery.LOGGER.info(
                "Manifested dynamic case {} {} at {}",
                profile.instanceId(), role, position);
    }

    private static void ensureEvidenceDisplay(
            ServerLevel level, BlockPos position, DynamicCaseProfile profile) {
        ArmorStand existing = level.getEntitiesOfClass(
                        ArmorStand.class,
                        new AABB(position).inflate(SEARCH_RANGE),
                        display -> display.getTags().contains(EVIDENCE_TAG)
                                && profile.instanceId().equals(display
                                        .getPersistentData()
                                        .getString(INSTANCE_DATA)))
                .stream().findFirst().orElse(null);
        if (existing != null) return;
        ArmorStand display = EntityType.ARMOR_STAND.create(level);
        if (display == null) return;
        display.moveTo(position.getX() + 0.5d, position.getY() + 0.35d,
                position.getZ() + 0.5d, 0f, 0f);
        display.setItemSlot(
                EquipmentSlot.MAINHAND, DynamicCaseEvidenceItem.create(profile));
        display.setInvulnerable(true);
        display.setNoGravity(true);
        display.setInvisible(true);
        display.setShowArms(true);
        display.setNoBasePlate(true);
        display.addTag(EVIDENCE_TAG);
        display.getPersistentData().putString(
                INSTANCE_DATA, profile.instanceId());
        display.getPersistentData().putString(ROLE_DATA, "evidence");
        level.addFreshEntity(display);
        ProjectMystery.LOGGER.info(
                "Manifested dynamic case {} evidence at {}",
                profile.instanceId(), position);
    }

    private static void cleanupInactive(
            ServerLevel level,
            Set<BlockPos> activeAnchors,
            Set<String> activeInstances) {
        for (BlockPos anchor : activeAnchors) {
            AABB area = new AABB(anchor).inflate(SEARCH_RANGE);
            level.getEntitiesOfClass(Villager.class, area,
                            DynamicCaseManifestationService::isManifestationNpc)
                    .stream()
                    .filter(villager -> !activeInstances.contains(villager
                            .getPersistentData().getString(INSTANCE_DATA)))
                    .forEach(Villager::discard);
            level.getEntitiesOfClass(ArmorStand.class, area,
                            display -> display.getTags().contains(EVIDENCE_TAG))
                    .stream()
                    .filter(display -> !activeInstances.contains(display
                            .getPersistentData().getString(INSTANCE_DATA)))
                    .forEach(ArmorStand::discard);
        }
    }

    private static Component subjectName(DynamicCaseProfile profile) {
        return Component.translatable(
                "entity.lord_of_mysteries.dynamic_case_subject",
                Component.translatable(profile.subject().translationKey("subject")));
    }

    private static Component affectedName(
            DynamicCaseProfile.VictimImpact impact) {
        return Component.translatable(
                "entity.lord_of_mysteries.dynamic_case_affected." + impact.id());
    }

    private static VillagerProfession subjectProfession(
            DynamicCaseProfile.Subject subject) {
        return switch (subject) {
            case APPRENTICE_REPORTER -> VillagerProfession.CARTOGRAPHER;
            case DOCK_ACCOUNTANT -> VillagerProfession.LIBRARIAN;
            case HERBALIST_ASSISTANT -> VillagerProfession.FARMER;
            case RETIRED_CONSTABLE -> VillagerProfession.WEAPONSMITH;
        };
    }

    private static VillagerProfession affectedProfession(
            DynamicCaseProfile.VictimImpact impact) {
        return switch (impact) {
            case FAMILY_PANIC -> VillagerProfession.NITWIT;
            case DISTRICT_SHORTAGE -> VillagerProfession.BUTCHER;
            case GUARD_CRACKDOWN -> VillagerProfession.LEATHERWORKER;
            case OCCULT_RUMOR -> VillagerProfession.LIBRARIAN;
        };
    }

    private static int verticalOffset(
            DynamicCaseProfile.CaseLocation location) {
        return switch (location) {
            case MIST_CITY_OUTPOST, OCCULTIST_HUT -> 1;
            case ABANDONED_CHURCH, CULTIST_CAMP -> 0;
        };
    }

    private static BlockPos findOpenPosition(
            ServerLevel level, BlockPos preferred, Set<BlockPos> reserved) {
        for (int radius = 0; radius <= 3; radius++) {
            for (int deltaX = -radius; deltaX <= radius; deltaX++) {
                for (int deltaZ = -radius; deltaZ <= radius; deltaZ++) {
                    if (Math.max(Math.abs(deltaX), Math.abs(deltaZ)) != radius) {
                        continue;
                    }
                    BlockPos candidate = preferred.offset(deltaX, 0, deltaZ);
                    boolean separated = reserved.stream().allMatch(position ->
                            position.distSqr(candidate) > 2d);
                    if (separated
                            && level.getBlockState(candidate).isAir()
                            && level.getBlockState(candidate.above()).isAir()
                            && !level.getBlockState(candidate.below()).isAir()) {
                        return candidate;
                    }
                }
            }
        }
        return preferred;
    }

    private static Optional<BlockPos> manifestationTarget(
            ServerLevel level, DynamicCaseProfile.CaseLocation location) {
        InvestigationSiteSavedData sites = InvestigationSiteSavedData.get(level);
        return switch (location) {
            case MIST_CITY_OUTPOST -> MistCityOutpostSavedData.get(level).outpost();
            case ABANDONED_CHURCH -> sites.church();
            case CULTIST_CAMP -> sites.cultistCamp();
            case OCCULTIST_HUT -> sites.occultistHut();
        };
    }
}
