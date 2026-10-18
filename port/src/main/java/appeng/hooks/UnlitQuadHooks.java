/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.hooks;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.util.GsonHelper;

/**
 * FABRIC: Implements NeoForge's per-face light emission in block models ("neoforge_data": {"block_light": N,
 * "sky_light": M}), which AE2 uses for glowing indicators. The deserialized face is remembered, and when it is baked
 * the light value is written into the quad's lightmap coordinates. Both vanilla and the Fabric renderer use the
 * maximum of the quad's lightmap and the world light, which gives the same result as NeoForge.
 */
public final class UnlitQuadHooks {
    private static final int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    private static final int LIGHT_OFFSET = DefaultVertexFormat.BLOCK.getOffset(VertexFormatElement.UV2) / 4;

    // Faces are records, so equal faces from different models must not be confused: weak keys use identity.
    private static final Map<BlockElementFace, Integer> EMISSIVE_FACES = new MapMaker().weakKeys().makeMap();

    private UnlitQuadHooks() {
    }

    public static void onFaceDeserialized(BlockElementFace face, JsonElement json) {
        if (!json.isJsonObject()) {
            return;
        }
        var obj = json.getAsJsonObject();
        if (!obj.has("neoforge_data")) {
            return;
        }
        var data = GsonHelper.getAsJsonObject(obj, "neoforge_data");
        int blockLight = GsonHelper.getAsInt(data, "block_light", 0);
        int skyLight = GsonHelper.getAsInt(data, "sky_light", 0);
        if (blockLight > 0 || skyLight > 0) {
            EMISSIVE_FACES.put(face, LightTexture.pack(blockLight, skyLight));
        }
    }

    public static BakedQuad onFaceBaked(BlockElementFace face, BakedQuad quad) {
        var light = EMISSIVE_FACES.get(face);
        if (light == null) {
            return quad;
        }
        int[] vertexData = quad.getVertices().clone();
        for (int i = 0; i < 4; i++) {
            vertexData[STRIDE * i + LIGHT_OFFSET] = light;
        }
        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }
}
