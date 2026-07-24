package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DynamicCaseGeneratorTest {

    @Test
    void generationIsDeterministicAndKeepsAllEightSlots() {
        DynamicCaseProfile first = DynamicCaseGenerator.generateForDay(
                0x1234_5678_9ABCL, 42L);
        DynamicCaseProfile second = DynamicCaseGenerator.generateForDay(
                0x1234_5678_9ABCL, 42L);

        assertEquals(first, second);
        assertEquals(42L, first.caseDay());
        assertEquals(6L, first.caseWeek());
        assertTrue(first.instanceId().startsWith(first.archetype().id()));
        assertTrue(first.conclusion().supports(first.method()));
        assertTrue(first.relationship().supports(first.subject()));
        assertTrue(first.schedule().supports(first.subject()));
        assertTrue(first.organization().reputationPath()
                .startsWith("organization/"));
        assertTrue(first.subject().translationKey("subject").contains(
                first.subject().id()));
        assertTrue(first.motive().translationKey("motive").contains(
                first.motive().id()));
        assertTrue(first.location().translationKey("location").contains(
                first.location().id()));
        assertTrue(first.anomaly().translationKey("anomaly").contains(
                first.anomaly().id()));
        assertTrue(first.coverUp().translationKey("cover_up").contains(
                first.coverUp().id()));
        assertTrue(first.victimImpact().translationKey("victim_impact").contains(
                first.victimImpact().id()));
        assertTrue(first.evidenceTheme().translationKey("evidence_theme").contains(
                first.evidenceTheme().id()));
    }

    @Test
    void consecutiveDaysRotateAcrossAllThreeArchetypes() {
        Set<DynamicCaseProfile.Archetype> archetypes = new HashSet<>();
        for (long day = 20L; day < 23L; day++) {
            archetypes.add(DynamicCaseGenerator.generateForDay(9918273L, day)
                    .archetype());
        }

        assertEquals(Set.of(DynamicCaseProfile.Archetype.values()), archetypes);
    }

    @Test
    void generatedMethodsAlwaysExplainTheirConclusions() {
        Set<DynamicCaseProfile.Conclusion> conclusions = new HashSet<>();
        Set<String> instances = new HashSet<>();
        for (long day = 0L; day < 256L; day++) {
            DynamicCaseProfile profile = DynamicCaseGenerator.generateForDay(
                    7348291023L, day);
            conclusions.add(profile.conclusion());
            instances.add(profile.instanceId());
            assertTrue(profile.conclusion().supports(profile.method()));
        }

        assertEquals(Set.of(DynamicCaseProfile.Conclusion.values()), conclusions);
        assertEquals(256, instances.size());
    }

    @Test
    void consecutiveWeeksRotateAcrossCoordinatingOrganizations() {
        Set<DynamicCaseProfile.Organization> organizations = new HashSet<>();
        for (long week = 0L; week < 3L; week++) {
            DynamicCaseProfile profile = DynamicCaseGenerator.generateForDay(
                    9918273L, week * 7L);
            organizations.add(profile.organization());
            assertEquals(week, profile.caseWeek());
        }

        assertEquals(Set.of(DynamicCaseProfile.Organization.values()),
                organizations);
    }

    @Test
    void everySubjectKeepsItsRelationshipAndScheduleAcrossDays() {
        Set<DynamicCaseProfile.Subject> subjects = new HashSet<>();
        for (long day = 0L; day < 128L; day++) {
            DynamicCaseProfile profile = DynamicCaseGenerator.generateForDay(
                    3498127L, day);
            subjects.add(profile.subject());
            assertTrue(profile.relationship().supports(profile.subject()));
            assertTrue(profile.schedule().supports(profile.subject()));
        }

        assertEquals(Set.of(DynamicCaseProfile.Subject.values()), subjects);
    }
}
