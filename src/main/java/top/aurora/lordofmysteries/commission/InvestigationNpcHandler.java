package top.aurora.lordofmysteries.commission;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.world.InvestigationSiteSavedData;
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InvestigationNpcHandler {

    public static final String PRESS_CLERK_TAG = "lom_press_clerk";
    public static final String NIGHTHAWK_CONTACT_TAG = "lom_nighthawk_contact";
    public static final String MISSING_REPORTER_TAG = "lom_missing_reporter";
    public static final String OCCULT_APPRAISER_TAG = "lom_occult_appraiser";
    public static final String ESCORT_OWNER_TAG = "lom_escort_owner";

    private InvestigationNpcHandler() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)
                || !isInvestigationNpc(villager)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(
                event.getEntity().level().isClientSide()));
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (villager.getTags().contains(PRESS_CLERK_TAG)) {
            if (CityLifeService.tryWorkPressShift(player)) return;
            CommissionService.talkPressClerk(player);
        } else if (villager.getTags().contains(NIGHTHAWK_CONTACT_TAG)) {
            CommissionService.interactContact(player);
        } else if (villager.getTags().contains(MISSING_REPORTER_TAG)) {
            CommissionService.rescueReporter(player, villager);
        } else if (villager.getTags().contains(OCCULT_APPRAISER_TAG)) {
            CommissionService.interactOccultAppraiser(player);
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
    }

    public static void beginEscort(Villager reporter, ServerPlayer owner) {
        reporter.setNoAi(false);
        reporter.setInvulnerable(true);
        reporter.setPersistenceRequired();
        reporter.getPersistentData().putUUID(ESCORT_OWNER_TAG, owner.getUUID());
    }

    public static void finishEscort(Villager reporter, BlockPos outpost) {
        reporter.getNavigation().stop();
        reporter.setNoAi(true);
        reporter.moveTo(outpost.getX() + 0.5d, outpost.getY() + 1d,
                outpost.getZ() + 2.5d, reporter.getYRot(), reporter.getXRot());
    }

    private static boolean isInvestigationNpc(Villager villager) {
        return villager.getTags().contains(PRESS_CLERK_TAG)
                || villager.getTags().contains(NIGHTHAWK_CONTACT_TAG)
                || villager.getTags().contains(MISSING_REPORTER_TAG)
                || villager.getTags().contains(OCCULT_APPRAISER_TAG);
    }

    private static void ensureOutpostNpcs(ServerLevel level, BlockPos outpost) {
        ensureVillager(level, outpost.offset(-2, 1, -1), PRESS_CLERK_TAG,
                "entity.lord_of_mysteries.press_clerk",
                VillagerProfession.LIBRARIAN, villager -> villager.setNoAi(true));
        ensureVillager(level, outpost.offset(2, 1, -1), NIGHTHAWK_CONTACT_TAG,
                "entity.lord_of_mysteries.nighthawk_contact",
                VillagerProfession.CLERIC, villager -> villager.setNoAi(true));
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
                        Villager.class, new AABB(position).inflate(12d),
                        villager -> villager.getTags().contains(tag))
                .stream().findFirst().orElse(null);
        if (existing != null) return existing;
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
