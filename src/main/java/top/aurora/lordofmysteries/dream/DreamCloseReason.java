package top.aurora.lordofmysteries.dream;

public enum DreamCloseReason {
    COMPLETE("complete", false),
    VOLUNTARY("voluntary", false),
    INVITE_TIMEOUT("invite_timeout", false),
    SESSION_TIMEOUT("session_timeout", true),
    DECLINED("declined", false),
    DISCONNECTED("disconnected", true),
    WRONG_DIMENSION("wrong_dimension", true),
    DREAM_DEATH("dream_death", true),
    TEAM_CHANGED("team_changed", true),
    ORGANIZATION_ACCESS_LOST("organization_access_lost", true),
    INTERRUPTED("interrupted", true);

    private final String id;
    private final boolean traumatic;

    DreamCloseReason(String id, boolean traumatic) {
        this.id = id;
        this.traumatic = traumatic;
    }

    public String id() {
        return id;
    }

    public boolean traumatic() {
        return traumatic;
    }

    public static DreamCloseReason fromId(String id) {
        if (id == null) return null;
        for (DreamCloseReason reason : values()) {
            if (reason.id.equalsIgnoreCase(id)) return reason;
        }
        return null;
    }
}
