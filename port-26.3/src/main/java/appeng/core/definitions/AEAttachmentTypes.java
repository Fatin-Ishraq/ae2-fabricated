package appeng.core.definitions;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import appeng.core.AppEng;

/**
 * FABRIC: Uses the Fabric Data Attachment API instead of NeoForge attachment types.
 */
public final class AEAttachmentTypes {
    public static final AttachmentType<Boolean> HOLDING_CTRL = AttachmentRegistry
            .<Boolean>create(AppEng.makeId("ctrl"), builder -> builder.initializer(() -> false));

    private AEAttachmentTypes() {
    }

    public static void register() {
        // Triggers class initialization
    }
}
