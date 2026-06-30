package top.aurora.projectmystery.player;

import java.util.function.Supplier;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 玩家附件注册（设计文档 §5.1）。
 *
 * NeoForge 1.21.1 用 AttachmentType 把 {@link PlayerMysteryData} 附着到玩家实体，
 * copyOnDeath 保证死亡后默认保留非凡者数据（是否重置由 breakdown_mode 配置另行处理）。
 */
public final class MysteryAttachments {

    private MysteryAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ProjectMystery.MOD_ID);

    public static final Supplier<AttachmentType<PlayerMysteryData>> MYSTERY_DATA =
            ATTACHMENT_TYPES.register("mystery_data", () ->
                    AttachmentType.builder(PlayerMysteryData::new)
                            .serialize(PlayerMysteryData.CODEC)
                            .copyOnDeath()
                            .build()
            );
}
