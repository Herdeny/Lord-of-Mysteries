package top.aurora.lordofmysteries.characteristic;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CharacteristicConservationService {

    private static final String ENTITY_PAYLOAD =
            "project_mystery_characteristic_bundle";
    private static final String STACK_PAYLOAD = "CharacteristicBundle";

    private CharacteristicConservationService() {}

    public static boolean transferCurrentBundle(
            Mob carrier, PlayerMysteryData data) {
        Optional<CharacteristicBundle> extracted = extractCurrentBundle(data);
        if (extracted.isEmpty()) return false;
        carrier.getPersistentData().put(
                ENTITY_PAYLOAD, extracted.get().save());
        return true;
    }

    public static Optional<CharacteristicBundle> extractCurrentBundle(
            PlayerMysteryData data) {
        if (data.pathway == null) return Optional.empty();
        for (int index = 0; index < data.characteristicBundles.size(); index++) {
            CharacteristicBundle bundle = data.characteristicBundles.get(index);
            if (bundle.pathway().equals(data.pathway)) {
                data.characteristicBundles.remove(index);
                return Optional.of(bundle);
            }
        }
        return Optional.empty();
    }

    public static Optional<CharacteristicBundle> readCarrier(Entity entity) {
        if (!entity.getPersistentData().contains(ENTITY_PAYLOAD)) {
            return Optional.empty();
        }
        return load(entity.getPersistentData().getCompound(ENTITY_PAYLOAD));
    }

    public static ItemStack createStack(CharacteristicBundle bundle) {
        ItemStack stack = new ItemStack(ModItems.BROKEN_CHARACTERISTIC.get());
        stack.getOrCreateTag().put(STACK_PAYLOAD, bundle.save());
        return stack;
    }

    public static Optional<CharacteristicBundle> readStack(ItemStack stack) {
        if (!stack.is(ModItems.BROKEN_CHARACTERISTIC.get())
                || stack.getTag() == null
                || !stack.getTag().contains(STACK_PAYLOAD)) {
            return Optional.empty();
        }
        return load(stack.getTag().getCompound(STACK_PAYLOAD));
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        Optional<CharacteristicBundle> bundle = readCarrier(event.getEntity());
        if (bundle.isEmpty()) return;
        event.getDrops().removeIf(drop ->
                drop.getItem().is(ModItems.BROKEN_CHARACTERISTIC.get()));
        event.getDrops().add(new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                createStack(bundle.get())));
    }

    private static Optional<CharacteristicBundle> load(CompoundTag tag) {
        try {
            return Optional.of(CharacteristicBundle.load(tag));
        } catch (IllegalArgumentException exception) {
            ProjectMystery.LOGGER.warn(
                    "Rejected invalid characteristic payload", exception);
            return Optional.empty();
        }
    }
}
