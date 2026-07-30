package top.aurora.lordofmysteries.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.entity.SeerBreakdownEntity;
import top.aurora.lordofmysteries.entity.ShapeshifterSerpentEntity;
import top.aurora.lordofmysteries.entity.SpiritWispEntity;
import top.aurora.lordofmysteries.entity.AshenPuppetEntity;
import top.aurora.lordofmysteries.entity.AbilityLeechEntity;
import top.aurora.lordofmysteries.entity.CradleMothEntity;
import top.aurora.lordofmysteries.entity.MarionetteVineEntity;
import top.aurora.lordofmysteries.entity.RiftlingEntity;
import top.aurora.lordofmysteries.entity.ScribeGolemEntity;
import top.aurora.lordofmysteries.entity.ThiefBreakdownEntity;
import top.aurora.lordofmysteries.entity.ApprenticeBreakdownEntity;
import top.aurora.lordofmysteries.entity.PsychiatristBreakdownEntity;
import top.aurora.lordofmysteries.entity.PyromaniacBreakdownEntity;
import top.aurora.lordofmysteries.entity.TravelerDoorEntity;
import top.aurora.lordofmysteries.entity.TwinSerpentEntity;
import top.aurora.lordofmysteries.entity.WarRoseHuskEntity;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {

    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ProjectMystery.MOD_ID);

    public static final RegistryObject<EntityType<SeerBreakdownEntity>> SEER_BREAKDOWN =
            ENTITIES.register("seer_breakdown", () ->
                    EntityType.Builder.of(SeerBreakdownEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":seer_breakdown"));

    public static final RegistryObject<EntityType<ThiefBreakdownEntity>> THIEF_BREAKDOWN =
            ENTITIES.register("thief_breakdown", () ->
                    EntityType.Builder.of(ThiefBreakdownEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":thief_breakdown"));

    public static final RegistryObject<EntityType<ApprenticeBreakdownEntity>>
            APPRENTICE_BREAKDOWN = ENTITIES.register("apprentice_breakdown", () ->
                    EntityType.Builder.of(
                                    ApprenticeBreakdownEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.6f, 2.9f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":apprentice_breakdown"));

    public static final RegistryObject<EntityType<PsychiatristBreakdownEntity>>
            PSYCHIATRIST_BREAKDOWN = ENTITIES.register(
                    "psychiatrist_breakdown", () ->
                            EntityType.Builder.of(
                                            PsychiatristBreakdownEntity::new,
                                            MobCategory.MONSTER)
                                    .sized(0.6f, 1.95f)
                                    .clientTrackingRange(8)
                                    .build(ProjectMystery.MOD_ID
                                            + ":psychiatrist_breakdown"));

    public static final RegistryObject<EntityType<PyromaniacBreakdownEntity>>
            PYROMANIAC_BREAKDOWN = ENTITIES.register(
                    "pyromaniac_breakdown", () ->
                            EntityType.Builder.of(
                                            PyromaniacBreakdownEntity::new,
                                            MobCategory.MONSTER)
                                    .sized(0.6f, 1.95f)
                                    .fireImmune()
                                    .clientTrackingRange(8)
                                    .build(ProjectMystery.MOD_ID
                                            + ":pyromaniac_breakdown"));

    public static final RegistryObject<EntityType<ShapeshifterSerpentEntity>>
            SHAPESHIFTER_SERPENT = ENTITIES.register("shapeshifter_serpent", () ->
                    EntityType.Builder.of(ShapeshifterSerpentEntity::new, MobCategory.MONSTER)
                            .sized(0.72f, 0.5f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":shapeshifter_serpent"));

    public static final RegistryObject<EntityType<SpiritWispEntity>> SPIRIT_WISP =
            ENTITIES.register("spirit_wisp", () ->
                    EntityType.Builder.of(SpiritWispEntity::new, MobCategory.MONSTER)
                            .sized(0.4f, 0.8f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":spirit_wisp"));

    public static final RegistryObject<EntityType<AshenPuppetEntity>> ASHEN_PUPPET =
            ENTITIES.register("ashen_puppet", () ->
                    EntityType.Builder.of(AshenPuppetEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":ashen_puppet"));

    public static final RegistryObject<EntityType<MarionetteVineEntity>>
            MARIONETTE_VINE = ENTITIES.register("marionette_vine", () ->
                    EntityType.Builder.of(
                                    MarionetteVineEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.9f, 0.65f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":marionette_vine"));

    public static final RegistryObject<EntityType<CradleMothEntity>> CRADLE_MOTH =
            ENTITIES.register("cradle_moth", () ->
                    EntityType.Builder.of(CradleMothEntity::new, MobCategory.MONSTER)
                            .sized(0.55f, 0.8f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":cradle_moth"));

    public static final RegistryObject<EntityType<TwinSerpentEntity>> TWIN_SERPENT =
            ENTITIES.register("twin_serpent", () ->
                    EntityType.Builder.of(TwinSerpentEntity::new, MobCategory.MONSTER)
                            .sized(0.82f, 0.55f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":twin_serpent"));

    public static final RegistryObject<EntityType<AbilityLeechEntity>> ABILITY_LEECH =
            ENTITIES.register("ability_leech", () ->
                    EntityType.Builder.of(AbilityLeechEntity::new, MobCategory.MONSTER)
                            .sized(0.45f, 0.3f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":ability_leech"));

    public static final RegistryObject<EntityType<ScribeGolemEntity>> SCRIBE_GOLEM =
            ENTITIES.register("scribe_golem", () ->
                    EntityType.Builder.of(ScribeGolemEntity::new, MobCategory.MONSTER)
                            .sized(0.7f, 2.1f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":scribe_golem"));

    public static final RegistryObject<EntityType<RiftlingEntity>> RIFTLING =
            ENTITIES.register("riftling", () ->
                    EntityType.Builder.of(RiftlingEntity::new, MobCategory.MONSTER)
                            .sized(0.45f, 0.35f)
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":riftling"));

    public static final RegistryObject<EntityType<WarRoseHuskEntity>> WAR_ROSE_HUSK =
            ENTITIES.register("war_rose_husk", () ->
                    EntityType.Builder.of(
                                    WarRoseHuskEntity::new,
                                    MobCategory.MONSTER)
                            .sized(0.65f, 2.05f)
                            .fireImmune()
                            .clientTrackingRange(8)
                            .build(ProjectMystery.MOD_ID + ":war_rose_husk"));

    public static final RegistryObject<EntityType<TravelerDoorEntity>>
            TRAVELER_DOOR = ENTITIES.register("traveler_door", () ->
                    EntityType.Builder.of(
                                    TravelerDoorEntity::new,
                                    MobCategory.MISC)
                            .sized(1.1f, 2.4f)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build(ProjectMystery.MOD_ID + ":traveler_door"));

    @SubscribeEvent
    public static void onCreateAttributes(EntityAttributeCreationEvent event) {
        event.put(SEER_BREAKDOWN.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .build());
        event.put(THIEF_BREAKDOWN.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .build());
        event.put(APPRENTICE_BREAKDOWN.get(), EnderMan.createAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .build());
        event.put(PSYCHIATRIST_BREAKDOWN.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 38.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 36.0)
                .build());
        event.put(PYROMANIAC_BREAKDOWN.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.FOLLOW_RANGE, 36.0)
                .build());
        event.put(SHAPESHIFTER_SERPENT.get(), Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .build());
        event.put(SPIRIT_WISP.get(), Vex.createAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .build());
        event.put(ASHEN_PUPPET.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .build());
        event.put(MARIONETTE_VINE.get(), Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .build());
        event.put(CRADLE_MOTH.get(), Vex.createAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .build());
        event.put(TWIN_SERPENT.get(), Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .build());
        event.put(ABILITY_LEECH.get(), Silverfish.createAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .build());
        event.put(SCRIBE_GOLEM.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 42.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .build());
        event.put(RIFTLING.get(), Endermite.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .build());
        event.put(WAR_ROSE_HUSK.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 38.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .build());
    }

    public static void registerSpawnPlacements() {
        SpawnPlacements.register(
                SHAPESHIFTER_SERPENT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                SPIRIT_WISP.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                ASHEN_PUPPET.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                MARIONETTE_VINE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                CRADLE_MOTH.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                TWIN_SERPENT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                ABILITY_LEECH.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                SCRIBE_GOLEM.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                RIFTLING.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
        SpawnPlacements.register(
                WAR_ROSE_HUSK.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules);
    }
}
