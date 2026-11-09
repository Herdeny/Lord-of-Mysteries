package top.aurora.lordofmysteries.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;

import java.util.List;

/**
 * 客户端按键（批次1）。
 *
 * <p>默认按键：
 * <ul>
 *   <li>{@code V}：切换灵视；</li>
 *   <li>{@code B}：简易占卜。</li>
 * </ul>
 * 玩家可在原版控制里改键；KeyMapping 会自动列出并生效。
 */
public final class PMKeyBindings {

    private PMKeyBindings() {}

    private static final String CATEGORY = "key.categories.lord_of_mysteries";

    public static final KeyMapping TOGGLE_SPIRIT_VISION = new KeyMapping(
            "key.lord_of_mysteries.toggle_spirit_vision",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            CATEGORY);

    public static final KeyMapping USE_DIVINATION = new KeyMapping(
            "key.lord_of_mysteries.use_divination",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_B,
            CATEGORY);

    public static final KeyMapping OPEN_STATUS = new KeyMapping(
            "key.lord_of_mysteries.open_status",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_N,
            CATEGORY);

    public static final KeyMapping TOGGLE_EMOTION_READ = new KeyMapping(
            "key.lord_of_mysteries.toggle_emotion_read",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            CATEGORY);

    public static final KeyMapping USE_SURFACE_READ = new KeyMapping(
            "key.lord_of_mysteries.use_surface_read",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            CATEGORY);

    public static final KeyMapping USE_MENTAL_SUGGESTION = new KeyMapping(
            "key.lord_of_mysteries.use_mental_suggestion",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_J,
            CATEGORY);

    public static final KeyMapping USE_PROVOKE = new KeyMapping(
            "key.lord_of_mysteries.use_provoke",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            CATEGORY);

    public static final KeyMapping USE_ENRAGE = new KeyMapping(
            "key.lord_of_mysteries.use_enrage",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_I,
            CATEGORY);

    public static final KeyMapping USE_CARD_BLADE = new KeyMapping(
            "key.lord_of_mysteries.use_card_blade",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            CATEGORY);

    public static final KeyMapping USE_FLAME_LEAP = new KeyMapping(
            "key.lord_of_mysteries.use_flame_leap",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            CATEGORY);

    public static final KeyMapping ARM_PAPER_SUBSTITUTE = new KeyMapping(
            "key.lord_of_mysteries.arm_paper_substitute",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            CATEGORY);

    public static final KeyMapping USE_AIR_BULLET = new KeyMapping(
            "key.lord_of_mysteries.use_air_bullet",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            CATEGORY);

    public static final KeyMapping USE_STAGE_ILLUSION = new KeyMapping(
            "key.lord_of_mysteries.use_stage_illusion",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_M,
            CATEGORY);

    public static final KeyMapping USE_M2_PRIMARY = new KeyMapping(
            "key.lord_of_mysteries.use_m2_primary",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Y,
            CATEGORY);

    public static final KeyMapping USE_M2_SECONDARY = new KeyMapping(
            "key.lord_of_mysteries.use_m2_secondary",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_U,
            CATEGORY);

    public static final List<KeyMapping> ALL = List.of(
            TOGGLE_SPIRIT_VISION, USE_DIVINATION, OPEN_STATUS, TOGGLE_EMOTION_READ,
            USE_SURFACE_READ, USE_MENTAL_SUGGESTION, USE_PROVOKE, USE_ENRAGE,
            USE_CARD_BLADE, USE_FLAME_LEAP, ARM_PAPER_SUBSTITUTE, USE_AIR_BULLET,
            USE_STAGE_ILLUSION, USE_M2_PRIMARY, USE_M2_SECONDARY);

    public static void discardPendingClicks() {
        for (KeyMapping binding : ALL) {
            while (binding.consumeClick()) {}
        }
    }

    /** 通过 mod 总线注册；由 {@code ClientModEvents} 调用。 */
    public static void register(RegisterKeyMappingsEvent event) {
        ALL.forEach(event::register);
    }
}
