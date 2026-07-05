package top.aurora.lordofmysteries.knowledge;

public final class GuideJournalProgress {

    public static final int CHAPTER_COUNT = 9;

    private GuideJournalProgress() {}

    public static boolean isUnlocked(int chapter, boolean extraordinary,
                                     boolean seer, boolean hasM2Rumor,
                                     boolean hasGrayFogInvitation) {
        return switch (chapter) {
            case 1, 2, 4, 9 -> true;
            case 3, 6 -> extraordinary;
            case 5 -> seer;
            case 7 -> hasM2Rumor;
            case 8 -> hasGrayFogInvitation;
            default -> false;
        };
    }
}
