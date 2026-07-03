package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpectatorEmotionLogicTest {

    @Test
    void aggressionHasHighestPriority() {
        assertEquals(SpectatorEmotionLogic.Emotion.ANGER,
                SpectatorEmotionLogic.classify(true, 0.1f, true));
    }

    @Test
    void lowHealthReadsAsFear() {
        assertEquals(SpectatorEmotionLogic.Emotion.FEAR,
                SpectatorEmotionLogic.classify(false, 0.25f, true));
    }

    @Test
    void nearbyPassiveAttentionReadsAsCuriosity() {
        assertEquals(SpectatorEmotionLogic.Emotion.CURIOSITY,
                SpectatorEmotionLogic.classify(false, 0.8f, true));
    }

    @Test
    void neutralTargetReadsAsCalm() {
        assertEquals(SpectatorEmotionLogic.Emotion.CALM,
                SpectatorEmotionLogic.classify(false, 0.8f, false));
    }
}

