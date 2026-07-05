package top.aurora.lordofmysteries.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Zombie;
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

    @SubscribeEvent
    public static void onCreateAttributes(EntityAttributeCreationEvent event) {
        event.put(SEER_BREAKDOWN.get(), Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
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
    }
}
