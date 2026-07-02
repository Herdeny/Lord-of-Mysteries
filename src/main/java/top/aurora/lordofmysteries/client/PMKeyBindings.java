package top.aurora.lordofmysteries.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;

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

    /** 通过 mod 总线注册；由 {@code ClientModEvents} 调用。 */
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_SPIRIT_VISION);
        event.register(USE_DIVINATION);
        event.register(OPEN_STATUS);
    }
}
