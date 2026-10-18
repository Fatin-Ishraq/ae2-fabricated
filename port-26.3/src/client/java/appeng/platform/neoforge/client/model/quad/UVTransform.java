package appeng.platform.neoforge.client.model.quad;

import com.mojang.math.Quadrant;

import net.minecraft.client.model.geom.builders.UVPair;

/**
 * FABRIC: A rotation of sprite-space texture coordinates in 90 degree steps, optionally followed by mirroring.
 */
public final class UVTransform {
    private static final UVTransform[] ALL = new UVTransform[16];

    static {
        for (var quadrant : Quadrant.values()) {
            for (int flags = 0; flags < 4; flags++) {
                var transform = new UVTransform(quadrant, (flags & 1) != 0, (flags & 2) != 0);
                ALL[index(quadrant, transform.flipU, transform.flipV)] = transform;
            }
        }
    }

    public static final UVTransform IDENTITY = of(Quadrant.R0, false, false);

    private final Quadrant rotation;
    private final boolean flipU;
    private final boolean flipV;

    private UVTransform(Quadrant rotation, boolean flipU, boolean flipV) {
        this.rotation = rotation;
        this.flipU = flipU;
        this.flipV = flipV;
    }

    private static int index(Quadrant rotation, boolean flipU, boolean flipV) {
        return rotation.ordinal() | (flipU ? 4 : 0) | (flipV ? 8 : 0);
    }

    public static UVTransform of(Quadrant rotation, boolean flipU, boolean flipV) {
        return ALL[index(rotation, flipU, flipV)];
    }

    public boolean isIdentity() {
        return this == IDENTITY;
    }

    /**
     * Transforms sprite-space coordinates (0..1) packed with {@link UVPair#pack}.
     */
    public long transformPacked(long packedUv) {
        float u = UVPair.unpackU(packedUv);
        float v = UVPair.unpackV(packedUv);
        float newU;
        float newV;
        switch (rotation) {
            case R90 -> {
                newU = v;
                newV = 1 - u;
            }
            case R180 -> {
                newU = 1 - u;
                newV = 1 - v;
            }
            case R270 -> {
                newU = 1 - v;
                newV = u;
            }
            default -> {
                newU = u;
                newV = v;
            }
        }
        if (flipU) {
            newU = 1 - newU;
        }
        if (flipV) {
            newV = 1 - newV;
        }
        return UVPair.pack(newU, newV);
    }
}
