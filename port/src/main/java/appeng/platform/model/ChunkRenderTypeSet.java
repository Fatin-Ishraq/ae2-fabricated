/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.RenderType;

/**
 * FABRIC: Minimal equivalent of NeoForge's ChunkRenderTypeSet, an ordered set of chunk render types.
 */
public final class ChunkRenderTypeSet implements Iterable<RenderType> {
    private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(Set.of());

    private final Set<RenderType> types;

    private ChunkRenderTypeSet(Set<RenderType> types) {
        this.types = types;
    }

    public static ChunkRenderTypeSet none() {
        return NONE;
    }

    public static ChunkRenderTypeSet all() {
        return of(RenderType.chunkBufferLayers());
    }

    public static ChunkRenderTypeSet of(RenderType... types) {
        return of(List.of(types));
    }

    public static ChunkRenderTypeSet of(Collection<RenderType> types) {
        return new ChunkRenderTypeSet(new LinkedHashSet<>(types));
    }

    public static ChunkRenderTypeSet union(ChunkRenderTypeSet... sets) {
        var result = new LinkedHashSet<RenderType>();
        for (var set : sets) {
            result.addAll(set.types);
        }
        return new ChunkRenderTypeSet(result);
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }

    public boolean contains(RenderType renderType) {
        return types.contains(renderType);
    }

    public List<RenderType> asList() {
        return List.copyOf(types);
    }

    @Override
    public Iterator<RenderType> iterator() {
        return types.iterator();
    }
}
