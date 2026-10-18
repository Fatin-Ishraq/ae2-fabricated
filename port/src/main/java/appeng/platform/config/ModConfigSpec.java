/*
 * Unofficial Fabric port of Applied Energistics 2.
 * Copyright (c) 2026, Fabric port contributors.
 *
 * Licensed under the GNU Lesser General Public License v3.0 (see LICENSE).
 */

package appeng.platform.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FABRIC: A small re-implementation of the subset of NeoForge's ModConfigSpec that AE2 uses. Values are persisted as
 * a simple TOML file with the same layout NeoForge produces (one table per pushed section), so configuration files
 * can be moved between the NeoForge and Fabric versions.
 * <p>
 * Supported value types: boolean, int, double (with ranges), and enums.
 */
public final class ModConfigSpec {
    private static final Logger LOG = LoggerFactory.getLogger(ModConfigSpec.class);

    private final List<ConfigValue<?>> values;
    @Nullable
    private Path file;

    private ModConfigSpec(List<ConfigValue<?>> values) {
        this.values = values;
        for (var value : values) {
            value.spec = this;
        }
    }

    public List<ConfigValue<?>> getValues() {
        return values;
    }

    /**
     * Loads the config from the given file (creating it with defaults if missing) and remembers the file for
     * {@link #save()}.
     */
    public void load(Path file) {
        this.file = file;
        if (Files.exists(file)) {
            try {
                var parsed = parse(Files.readAllLines(file, StandardCharsets.UTF_8));
                for (var value : values) {
                    var raw = parsed.get(value.path);
                    if (raw != null) {
                        try {
                            value.setFromString(raw);
                        } catch (RuntimeException e) {
                            LOG.warn("Invalid value for {} in {}: {}. Using default.", value.path, file, raw);
                            value.reset();
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("Failed to read config file {}", file, e);
            }
        }
        // Always write back to add new keys/comments and normalize values
        save();
    }

    public void save() {
        if (file == null) {
            return;
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, serialize(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Failed to write config file {}", file, e);
        }
    }

    private String serialize() {
        var sb = new StringBuilder();
        String currentSection = null;
        for (var value : values) {
            var lastDot = value.path.lastIndexOf('.');
            var section = lastDot >= 0 ? value.path.substring(0, lastDot) : "";
            var key = lastDot >= 0 ? value.path.substring(lastDot + 1) : value.path;
            if (!section.equals(currentSection)) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                var sectionComment = value.sectionComment;
                if (sectionComment != null) {
                    appendComment(sb, sectionComment, "");
                }
                if (!section.isEmpty()) {
                    sb.append('[').append(section).append("]\n");
                }
                currentSection = section;
            }
            var indent = section.isEmpty() ? "" : "\t";
            if (value.comment != null) {
                appendComment(sb, value.comment, indent);
            }
            var rangeComment = value.rangeComment();
            if (rangeComment != null) {
                appendComment(sb, rangeComment, indent);
            }
            sb.append(indent).append(key).append(" = ").append(value.serialize()).append('\n');
        }
        return sb.toString();
    }

    private static void appendComment(StringBuilder sb, String comment, String indent) {
        for (var line : comment.split("\n")) {
            sb.append(indent).append('#').append(line.startsWith(" ") ? "" : " ").append(line).append('\n');
        }
    }

    private static Map<String, String> parse(List<String> lines) {
        var result = new LinkedHashMap<String, String>();
        var section = "";
        for (var line : lines) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).strip();
                continue;
            }
            var eq = line.indexOf('=');
            if (eq < 0) {
                continue;
            }
            var key = line.substring(0, eq).strip();
            var val = line.substring(eq + 1).strip();
            // Strip trailing comments outside of strings
            if (!val.startsWith("\"")) {
                var hash = val.indexOf('#');
                if (hash >= 0) {
                    val = val.substring(0, hash).strip();
                }
            } else {
                var end = val.indexOf('"', 1);
                if (end > 0) {
                    val = val.substring(1, end);
                }
            }
            result.put(section.isEmpty() ? key : section + "." + key, val);
        }
        return result;
    }

    public static final class Builder {
        private final Deque<String> path = new ArrayDeque<>();
        private final List<ConfigValue<?>> values = new ArrayList<>();
        @Nullable
        private String pendingComment;
        @Nullable
        private String pendingSectionComment;

        public Builder comment(String comment) {
            if (pendingComment == null) {
                pendingComment = comment;
            } else {
                pendingComment = pendingComment + "\n" + comment;
            }
            return this;
        }

        public Builder push(String name) {
            // A comment directly before push() documents the section
            pendingSectionComment = pendingComment;
            pendingComment = null;
            path.addLast(name);
            return this;
        }

        public Builder pop() {
            path.removeLast();
            return this;
        }

        private <T extends ConfigValue<?>> T add(String name, T value) {
            value.path = path.isEmpty() ? name : String.join(".", path) + "." + name;
            value.comment = pendingComment;
            value.sectionComment = pendingSectionComment;
            pendingComment = null;
            pendingSectionComment = null;
            values.add(value);
            return value;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return add(name, new BooleanValue(defaultValue));
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return add(name, new IntValue(defaultValue, min, max));
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
            return add(name, new DoubleValue(defaultValue, min, max));
        }

        public <T extends Enum<T>> EnumValue<T> defineEnum(String name, T defaultValue) {
            return add(name, new EnumValue<>(defaultValue));
        }

        public ModConfigSpec build() {
            return new ModConfigSpec(List.copyOf(values));
        }
    }

    public abstract static class ConfigValue<T> implements Supplier<T> {
        private final T defaultValue;
        protected T value;
        String path;
        @Nullable
        String comment;
        @Nullable
        String sectionComment;
        @Nullable
        ModConfigSpec spec;

        protected ConfigValue(T defaultValue) {
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        @Override
        public T get() {
            return value;
        }

        public T getDefault() {
            return defaultValue;
        }

        public void set(T value) {
            this.value = validate(value);
        }

        public void reset() {
            this.value = defaultValue;
        }

        public String getPath() {
            return path;
        }

        public void save() {
            if (spec != null) {
                spec.save();
            }
        }

        protected T validate(T value) {
            return value;
        }

        abstract void setFromString(String raw);

        String serialize() {
            return String.valueOf(value);
        }

        @Nullable
        String rangeComment() {
            return null;
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> implements BooleanSupplier {
        BooleanValue(boolean defaultValue) {
            super(defaultValue);
        }

        @Override
        public boolean getAsBoolean() {
            return value;
        }

        @Override
        void setFromString(String raw) {
            if (raw.equalsIgnoreCase("true")) {
                value = true;
            } else if (raw.equalsIgnoreCase("false")) {
                value = false;
            } else {
                throw new IllegalArgumentException(raw);
            }
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(int defaultValue, int min, int max) {
            super(defaultValue);
            this.min = min;
            this.max = max;
        }

        public int getAsInt() {
            return value;
        }

        @Override
        protected Integer validate(Integer value) {
            return Math.max(min, Math.min(max, value));
        }

        @Override
        void setFromString(String raw) {
            value = validate(Integer.parseInt(raw));
        }

        @Override
        String rangeComment() {
            if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
                return null;
            }
            return "Range: " + min + " ~ " + max;
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        DoubleValue(double defaultValue, double min, double max) {
            super(defaultValue);
            this.min = min;
            this.max = max;
        }

        public double getAsDouble() {
            return value;
        }

        @Override
        protected Double validate(Double value) {
            return Math.max(min, Math.min(max, value));
        }

        @Override
        void setFromString(String raw) {
            value = validate(Double.parseDouble(raw));
        }

        @Override
        String rangeComment() {
            if (min <= Double.MIN_VALUE && max == Double.MAX_VALUE) {
                return null;
            }
            return "Range: " + (max == Double.MAX_VALUE ? "> " + min : min + " ~ " + max);
        }
    }

    public static final class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        EnumValue(T defaultValue) {
            super(defaultValue);
        }

        @Override
        void setFromString(String raw) {
            var cls = getDefault().getDeclaringClass();
            for (var constant : cls.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(raw)) {
                    value = constant;
                    return;
                }
            }
            throw new IllegalArgumentException(raw);
        }

        @Override
        String serialize() {
            return "\"" + value.name() + "\"";
        }

        @Override
        String rangeComment() {
            var names = new ArrayList<String>();
            for (var constant : getDefault().getDeclaringClass().getEnumConstants()) {
                names.add(constant.name());
            }
            return "Allowed Values: " + String.join(", ", names).toUpperCase(Locale.ROOT).replace(" ", " ");
        }
    }
}
