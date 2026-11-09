package top.aurora.lordofmysteries.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public final class SleeplessShadeEntity extends Zombie {
    public SleeplessShadeEntity(EntityType<? extends SleeplessShadeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected boolean convertsInWater() { return false; }

    @Override
    protected boolean isSunSensitive() { return false; }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.PHANTOM_AMBIENT; }

    @Override
    protected float getSoundVolume() { return 0.25f; }
}
