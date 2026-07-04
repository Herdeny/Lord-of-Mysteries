package top.aurora.lordofmysteries.ritual;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

import top.aurora.lordofmysteries.registry.ModBlocks;

public final class MultiBlockRitualDetector {

    public static final int RADIUS = 3;
    private static final List<RitualStructureLogic.Offset> OFFSETS =
            RitualStructureLogic.circleOffsets(RADIUS);

    private MultiBlockRitualDetector() {}

    public static Inspection inspect(LevelReader level, BlockPos center) {
        int found = 0;
        for (RitualStructureLogic.Offset offset : OFFSETS) {
            if (level.getBlockState(center.offset(offset.x(), 0, offset.z()))
                    .is(ModBlocks.RITUAL_CHALK_MARK.get())) {
                found++;
            }
        }
        return new Inspection(found, OFFSETS.size());
    }

    public record Inspection(int found, int required) {
        public boolean complete() {
            return found == required;
        }

        public float completion() {
            return RitualStructureLogic.completion(found, required);
        }
    }
}
