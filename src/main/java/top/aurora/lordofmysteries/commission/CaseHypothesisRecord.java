package top.aurora.lordofmysteries.commission;

import java.util.regex.Pattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record CaseHypothesisRecord(
        String relationId,
        CaseHypothesisStance stance,
        String note,
        CaseHypothesisStatus status,
        int unresolvedStrain,
        int failedTests,
        int successfulTests,
        long lastTestTick) {

    public static final int MAX_NOTE_LENGTH = 160;
    public static final int MAX_STRAIN = 3;
    private static final int MAX_TEST_COUNT = 1_000_000;
    private static final Pattern RELATION_ID = Pattern.compile(
            "[a-z0-9][a-z0-9_./-]{0,63}");

    public static final CaseHypothesisRecord EMPTY = new CaseHypothesisRecord(
            "", CaseHypothesisStance.SUPPORTS, "",
            CaseHypothesisStatus.DRAFT, 0, 0, 0, -1L);

    public CaseHypothesisRecord {
        relationId = sanitizeRelationId(relationId);
        stance = stance == null ? CaseHypothesisStance.SUPPORTS : stance;
        note = sanitizeNote(note);
        status = status == null ? CaseHypothesisStatus.DRAFT : status;
        unresolvedStrain = clamp(unresolvedStrain, 0, MAX_STRAIN);
        failedTests = clamp(failedTests, 0, MAX_TEST_COUNT);
        successfulTests = clamp(successfulTests, 0, MAX_TEST_COUNT);
        lastTestTick = Math.max(-1L, lastTestTick);
        if (relationId.isBlank() || note.isBlank()) {
            relationId = "";
            note = "";
            status = CaseHypothesisStatus.DRAFT;
        }
    }

    public boolean hasDraft() {
        return !relationId.isBlank() && !note.isBlank();
    }

    public CaseHypothesisRecord propose(
            String relationId,
            CaseHypothesisStance stance,
            String note) {
        return new CaseHypothesisRecord(
                relationId, stance, note, CaseHypothesisStatus.DRAFT,
                unresolvedStrain, failedTests, successfulTests, lastTestTick);
    }

    public CaseHypothesisRecord clearDraft() {
        return new CaseHypothesisRecord(
                "", CaseHypothesisStance.SUPPORTS, "",
                CaseHypothesisStatus.DRAFT, unresolvedStrain,
                failedTests, successfulTests, lastTestTick);
    }

    CaseHypothesisRecord tested(
            CaseHypothesisStatus result,
            int strain,
            int failures,
            int successes,
            long gameTime) {
        return new CaseHypothesisRecord(
                relationId, stance, note, result, strain,
                failures, successes, gameTime);
    }

    CaseHypothesisRecord reconsidered(long gameTime) {
        return new CaseHypothesisRecord(
                "", CaseHypothesisStance.SUPPORTS, "",
                CaseHypothesisStatus.DRAFT,
                Math.max(0, unresolvedStrain - 1),
                failedTests, successfulTests, gameTime);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("relation_id", relationId);
        tag.putString("stance", stance.id());
        tag.putString("note", note);
        tag.putString("status", status.id());
        tag.putInt("unresolved_strain", unresolvedStrain);
        tag.putInt("failed_tests", failedTests);
        tag.putInt("successful_tests", successfulTests);
        tag.putLong("last_test_tick", lastTestTick);
        return tag;
    }

    public static CaseHypothesisRecord load(CompoundTag tag) {
        if (tag == null) throw new IllegalArgumentException("tag is required");
        return new CaseHypothesisRecord(
                tag.getString("relation_id"),
                CaseHypothesisStance.fromId(tag.getString("stance")),
                tag.getString("note"),
                CaseHypothesisStatus.fromId(tag.getString("status")),
                tag.getInt("unresolved_strain"),
                tag.getInt("failed_tests"),
                tag.getInt("successful_tests"),
                tag.getLong("last_test_tick"));
    }

    public static boolean isValid(CompoundTag tag) {
        if (tag == null
                || !tag.contains("relation_id", Tag.TAG_STRING)
                || !tag.contains("stance", Tag.TAG_STRING)
                || !tag.contains("note", Tag.TAG_STRING)
                || !tag.contains("status", Tag.TAG_STRING)
                || !tag.contains("unresolved_strain", Tag.TAG_INT)
                || !tag.contains("failed_tests", Tag.TAG_INT)
                || !tag.contains("successful_tests", Tag.TAG_INT)
                || !tag.contains("last_test_tick", Tag.TAG_LONG)) {
            return false;
        }
        String relationId = tag.getString("relation_id");
        String note = tag.getString("note");
        boolean draftShape = relationId.isBlank() == note.isBlank();
        return draftShape
                && (relationId.isBlank() || relationId.equals(
                        sanitizeRelationId(relationId)))
                && note.equals(sanitizeNote(note))
                && CaseHypothesisStance.fromId(tag.getString("stance")) != null
                && CaseHypothesisStatus.fromId(tag.getString("status")) != null
                && tag.getInt("unresolved_strain") >= 0
                && tag.getInt("unresolved_strain") <= MAX_STRAIN
                && tag.getInt("failed_tests") >= 0
                && tag.getInt("successful_tests") >= 0
                && tag.getLong("last_test_tick") >= -1L;
    }

    public static String sanitizeNote(String input) {
        if (input == null) return "";
        StringBuilder sanitized = new StringBuilder();
        boolean pendingSpace = false;
        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);
            if (character == '\u00a7') {
                if (index + 1 < input.length()) index++;
                continue;
            }
            if (Character.isWhitespace(character)) {
                pendingSpace = sanitized.length() > 0;
                continue;
            }
            if (Character.isISOControl(character)) continue;
            if (pendingSpace && sanitized.length() < MAX_NOTE_LENGTH) {
                sanitized.append(' ');
            }
            pendingSpace = false;
            if (sanitized.length() >= MAX_NOTE_LENGTH) break;
            sanitized.append(character);
        }
        return sanitized.toString().strip();
    }

    private static String sanitizeRelationId(String input) {
        if (input == null) return "";
        String candidate = input.strip();
        return RELATION_ID.matcher(candidate).matches() ? candidate : "";
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
