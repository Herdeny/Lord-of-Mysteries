package top.aurora.lordofmysteries.organization;

import java.util.List;

import net.minecraft.ChatFormatting;
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
import top.aurora.lordofmysteries.world.MistCityOutpostSavedData;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OrganizationLiaisonService {

    public static final String LIAISON_TAG = "lom_organization_liaison";
    private static final String SLOT_DATA = "lom_organization_slot";
    private static final String DAY_DATA = "lom_organization_day";
    private static final String ORGANIZATION_DATA = "lom_organization_id";
    private static final String ACTION_DATA = "lom_organization_action";

    private OrganizationLiaisonService() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.getServer().getTickCount() % 100 != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null || OrganizationDefinitionManager.all().isEmpty()) {
            return;
        }
        MistCityOutpostSavedData.get(level).outpost()
                .filter(level::hasChunkAt)
                .ifPresent(outpost -> ensureLiaisons(level, outpost));
    }

    @SubscribeEvent
    public static void onEntityInteract(
            PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)
                || !villager.getTags().contains(LIAISON_TAG)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(
                event.getEntity().level().isClientSide()));
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        interact(player, villager);
    }

    public static void ensureLiaisons(
            ServerLevel level, BlockPos outpost) {
        OrganizationActionSavedData saved =
                OrganizationActionService.refresh(level);
        List<OrganizationActionPolicy.PlannedAction> actions =
                saved.actions();
        List<Villager> liaisons = level.getEntitiesOfClass(
                Villager.class, new AABB(outpost).inflate(20d),
                villager -> villager.getTags().contains(LIAISON_TAG));
        for (int slot = 1;
             slot <= OrganizationActionPolicy.DAILY_ACTION_COUNT;
             slot++) {
            OrganizationActionPolicy.PlannedAction action =
                    saved.action(slot);
            if (action == null) continue;
            int currentSlot = slot;
            List<Villager> matching = liaisons.stream()
                    .filter(villager -> villager.getPersistentData()
                            .getInt(SLOT_DATA) == currentSlot)
                    .toList();
            Villager liaison = matching.stream().findFirst().orElse(null);
            matching.stream().skip(1).forEach(Villager::discard);
            BlockPos position = OrganizationLiaisonSchedulePolicy.position(
                    outpost, slot, level.getDayTime());
            if (!level.hasChunkAt(position)) continue;
            if (liaison == null) {
                liaison = createLiaison(level, position, slot);
            }
            if (liaison != null) {
                updateLiaison(liaison, position, action, saved.currentDay());
            }
        }
        liaisons.stream()
                .filter(villager -> {
                    int slot = villager.getPersistentData().getInt(SLOT_DATA);
                    return slot < 1 || slot > actions.size();
                })
                .forEach(Villager::discard);
    }

    private static void interact(
            ServerPlayer player, Villager liaison) {
        ServerLevel level = player.getServer().overworld();
        OrganizationActionSavedData saved =
                OrganizationActionService.refresh(level);
        int slot = liaison.getPersistentData().getInt(SLOT_DATA);
        OrganizationActionPolicy.PlannedAction action = saved.action(slot);
        if (action == null
                || liaison.getPersistentData().getLong(DAY_DATA)
                        != saved.currentDay()) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.organization.liaison_stale")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        OrganizationStrategyPolicy.Directive strategy =
                saved.strategy(action.organization());
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.organization.liaison_briefing",
                        OrganizationActionService.organizationName(
                                action.organization()),
                        Component.translatable(action.type().translationKey()),
                        action.risk(),
                        Component.translatable(
                                OrganizationLiaisonSchedulePolicy
                                        .shift(level.getDayTime())
                                        .translationKey()),
                        strategy == null
                                ? Component.literal("-")
                                : Component.translatable(
                                        strategy.focus().translationKey()))
                .withStyle(ChatFormatting.GOLD));
        OrganizationActionSavedData.Assignment assignment =
                saved.assignment(player.getUUID());
        if (assignment != null && assignment.slot() == slot) {
            OrganizationActionService.showActions(player);
            return;
        }
        OrganizationActionService.claim(player, slot);
    }

    private static Villager createLiaison(
            ServerLevel level, BlockPos position, int slot) {
        Villager liaison = EntityType.VILLAGER.create(level);
        if (liaison == null) return null;
        liaison.setVillagerData(liaison.getVillagerData()
                .setType(VillagerType.PLAINS)
                .setProfession(profession(slot))
                .setLevel(3));
        liaison.moveTo(position.getX() + 0.5d, position.getY(),
                position.getZ() + 0.5d, 0f, 0f);
        liaison.setPersistenceRequired();
        liaison.setInvulnerable(true);
        liaison.setNoAi(true);
        liaison.addTag(LIAISON_TAG);
        liaison.getPersistentData().putInt(SLOT_DATA, slot);
        if (!level.addFreshEntity(liaison)) return null;
        ProjectMystery.LOGGER.info(
                "Spawned organization liaison slot {} at {}",
                slot, position);
        return liaison;
    }

    private static void updateLiaison(
            Villager liaison, BlockPos position,
            OrganizationActionPolicy.PlannedAction action, long day) {
        liaison.getPersistentData().putLong(DAY_DATA, day);
        liaison.getPersistentData().putString(
                ORGANIZATION_DATA, action.organization().toString());
        liaison.getPersistentData().putString(
                ACTION_DATA, action.type().id());
        liaison.setCustomName(Component.translatable(
                "entity.lord_of_mysteries.organization_liaison",
                OrganizationActionService.organizationName(
                        action.organization())));
        liaison.setCustomNameVisible(true);
        liaison.setInvulnerable(true);
        liaison.setNoAi(true);
        if (liaison.distanceToSqr(
                position.getX() + 0.5d,
                position.getY(),
                position.getZ() + 0.5d) > 1d) {
            liaison.getNavigation().stop();
            liaison.moveTo(position.getX() + 0.5d, position.getY(),
                    position.getZ() + 0.5d,
                    liaison.getYRot(), liaison.getXRot());
        }
    }

    private static VillagerProfession profession(int slot) {
        return switch (slot) {
            case 1 -> VillagerProfession.CLERIC;
            case 2 -> VillagerProfession.CARTOGRAPHER;
            default -> VillagerProfession.WEAPONSMITH;
        };
    }
}
