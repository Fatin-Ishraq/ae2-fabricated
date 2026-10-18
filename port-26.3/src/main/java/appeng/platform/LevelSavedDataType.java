package appeng.platform;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * FABRIC: A saved data type whose constructor and codec need the level, which NeoForge supports by patching
 * {@link SavedDataType}. Vanilla saved data types compare by id, so creating one per lookup still uses the cached
 * instance of the level's data storage. The data fixer type is null; Fabric API supports that.
 */
public record LevelSavedDataType<T extends SavedData>(Identifier id, Factory<T> factory,
        Factory<Codec<T>> codecFactory) {

    public SavedDataType<T> forLevel(ServerLevel level) {
        return new SavedDataType<>(id, () -> factory.create(level), codecFactory.create(level), null);
    }

    @FunctionalInterface
    public interface Factory<T> {
        T create(ServerLevel level);
    }
}
