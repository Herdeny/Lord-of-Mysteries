package top.aurora.lordofmysteries.player;

public final class OccultActivityPolicy {

    private OccultActivityPolicy() {}

    public static Activity classify(
            boolean dreamDimension,
            DreamPhase dreamPhase,
            boolean spiritDimension,
            boolean spiritRecord) {
        DreamPhase normalizedDream = dreamPhase == null
                ? DreamPhase.NONE : dreamPhase;
        boolean dreamActivity = dreamDimension
                || normalizedDream != DreamPhase.NONE;
        boolean spiritActivity = spiritDimension || spiritRecord;
        if (dreamActivity && spiritActivity) return Activity.CONFLICT;
        if (dreamActivity) {
            return switch (normalizedDream) {
                case LOBBY -> Activity.DREAM_LOBBY;
                case ACTIVE -> Activity.DREAM_ACTIVE;
                case RECOVERY -> Activity.DREAM_RECOVERY;
                case NONE -> Activity.DREAM_ORPHANED;
            };
        }
        return spiritActivity
                ? Activity.SPIRIT_EXPEDITION : Activity.NONE;
    }

    public enum DreamPhase {
        NONE,
        LOBBY,
        ACTIVE,
        RECOVERY
    }

    public enum Activity {
        NONE("none", "none"),
        DREAM_LOBBY("dream_lobby", "dream_leave"),
        DREAM_ACTIVE("dream_active", "dream_exit"),
        DREAM_RECOVERY("dream_recovery", "dream_recover"),
        DREAM_ORPHANED("dream_orphaned", "dream_recover"),
        SPIRIT_EXPEDITION("spirit_expedition", "spirit_exit"),
        CONFLICT("conflict", "recover_conflict");

        private final String id;
        private final String recoveryHint;

        Activity(String id, String recoveryHint) {
            this.id = id;
            this.recoveryHint = recoveryHint;
        }

        public String id() {
            return id;
        }

        public String recoveryHint() {
            return recoveryHint;
        }

        public boolean available() {
            return this == NONE;
        }
    }
}
