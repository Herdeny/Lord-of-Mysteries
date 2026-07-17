package top.aurora.lordofmysteries.player;

import java.util.EnumSet;

/** Runtime synchronization partitions for player mystery data. */
public enum PlayerDataSection {
    CORE(1),
    KNOWLEDGE(1 << 1),
    SOCIAL(1 << 2),
    ENDGAME(1 << 3);

    public static final int ALL_MASK = EnumSet.allOf(PlayerDataSection.class)
            .stream()
            .mapToInt(PlayerDataSection::mask)
            .reduce(0, (left, right) -> left | right);

    private final int mask;

    PlayerDataSection(int mask) {
        this.mask = mask;
    }

    public int mask() {
        return mask;
    }
}
