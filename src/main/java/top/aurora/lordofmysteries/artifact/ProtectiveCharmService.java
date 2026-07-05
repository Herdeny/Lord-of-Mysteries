package top.aurora.lordofmysteries.artifact;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import top.aurora.lordofmysteries.registry.ModItems;

public final class ProtectiveCharmService {

    private ProtectiveCharmService() {}

    public static boolean tryAbsorbSevereEvent(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.PROTECTIVE_CHARM.get())) continue;
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.protective_charm.absorbed")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            ServerLevel level = player.serverLevel();
            level.sendParticles(player, ParticleTypes.ENCHANT, true,
                    player.getX(), player.getY() + 1d, player.getZ(),
                    28, 0.65d, 0.8d, 0.65d, 0.12d);
            level.playSound(null, player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 0.8f);
            return true;
        }
        return false;
    }
}
