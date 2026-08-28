package com.devinsterling.localize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ArgumentsHelper {
    // Static String variables to avoid repeating the same text throughout the nested classes
    private static final String MIXED_ARGS_NAMED= "Named argument given; mixing named and positional arguments is not supported: ";
    private static final String MIXED_ARGS_POSITIONAL = "Positional argument given; mixing named and positional arguments is not supported";
    private static final String NULL_RESOLVER_MESSAGE = "resolver must not be null";

    /// Cached strings to avoid int-to-string conversions.
    /// Typically, the size of arguments is small, so 0-9 is covered here.
    private static final String[] DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private final Arguments.Type backingHint;
    private MutableArguments arguments;

    public ArgumentsHelper(Arguments.Type backingHint) {
        this.backingHint = backingHint;
    }

    public void add(Object value) {
        if (arguments == null) {
            arguments = (backingHint == Arguments.Type.NAMED)
                    ? new PositionalMapArguments()
                    : new PositionalListArguments();
        }
        arguments.add(value);
    }

    public void add(String key, Object value) {
        if (arguments == null) {
            arguments = new NamedMapArguments();
        }
        arguments.add(key, value);
    }

    public Arguments snapshot() {
        return arguments == null ? Arguments.NONE : arguments.getUnmodifiableSnapshot();
    }

    public Arguments get() {
        return arguments == null ? Arguments.NONE : arguments.getUnmodifiable();
    }

    public static Arguments ofPositionalArguments(List<?> arguments) {
        return PositionalListArguments.ofUnmodifiable(arguments);
    }

    public static Arguments ofNamedArguments(Map<String, ?> arguments) {
        return NamedMapArguments.ofUnmodifiable(arguments);
    }

    private interface MutableArguments extends Arguments {
        void add(Object value);
        void add(String key, Object value);
        Arguments getUnmodifiableSnapshot();
        Arguments getUnmodifiable();
    }

    public static final class PositionalMapArguments extends MapArguments {

        private PositionalMapArguments() {
            super(new LinkedHashMap<>());
        }

        private PositionalMapArguments(Map<String, Object> arguments) {
            super(arguments);
        }

        @Override protected PositionalMapArguments createUnmodifiable(Map<String, ?> arguments) {
            return new PositionalMapArguments(Collections.unmodifiableMap(arguments));
        }

        @Override protected Map<String, Object> copyArgumentsMap() {
            return new LinkedHashMap<>(arguments);
        }

        @Override public void add(Object value) {
            arguments.put(intToString(arguments.size()), value);
        }

        @Override public void add(String key, Object value) {
            throw new IllegalStateException(MIXED_ARGS_NAMED + key);
        }

        @Override public Type type() {
            return Type.POSITIONAL;
        }
    }

    public static final class NamedMapArguments extends MapArguments {

        private NamedMapArguments() {
            super(new LinkedHashMap<>());
        }

        private NamedMapArguments(Map<String, Object> arguments) {
            super(arguments);
        }

        public static NamedMapArguments ofUnmodifiable(Map<String, ?> arguments) {
            return new NamedMapArguments(Collections.unmodifiableMap(arguments));
        }

        @Override protected NamedMapArguments createUnmodifiable(Map<String, ?> arguments) {
            return ofUnmodifiable(arguments);
        }

        @Override protected Map<String, Object> copyArgumentsMap() {
            return new LinkedHashMap<>(arguments);
        }

        @Override public void add(Object value) {
            throw new IllegalStateException(MIXED_ARGS_POSITIONAL);
        }

        @Override public void add(String key, Object value) {
            arguments.put(key, value);
        }

        @Override public Type type() {
            return Type.NAMED;
        }
    }

    private static abstract class MapArguments implements MutableArguments {
        protected final Map<String, Object> arguments;

        private MapArguments(Map<String, Object> arguments) {
            this.arguments = arguments;
        }

        protected abstract MapArguments createUnmodifiable(Map<String, ?> arguments);

        protected abstract Map<String, Object> copyArgumentsMap();

        @Override public Arguments getUnmodifiableSnapshot() {
            // `Map.copyOf` is not used here as argument values can be `null`
            return createUnmodifiable(copyArgumentsMap());
        }

        @Override public Arguments getUnmodifiable() {
            return createUnmodifiable(arguments);
        }

        @Override public int size() {
            return arguments.size();
        }

        @Override public Map<String, Object> toNamedMap() {
            return arguments;
        }

        @Override public Collection<Object> values() {
            return arguments.values();
        }

        @Override public Arguments resolve(Resolver resolver) {
            Objects.requireNonNull(resolver, NULL_RESOLVER_MESSAGE);
            Map<String, Object> resolved = null;

            for (Map.Entry<String, Object> entry : arguments.entrySet()) {
                String key = entry.getKey();
                Object originalValue = entry.getValue();
                Object resolvedValue = resolver.resolve(key, originalValue);

                if (!Objects.equals(originalValue, resolvedValue)) {
                    if (resolved == null) {
                        resolved = copyArgumentsMap();
                    }

                    resolved.put(key, resolvedValue);
                }
            }

            return resolved == null ? this : createUnmodifiable(resolved);
        }
    }

    public static final class PositionalListArguments implements MutableArguments {
        private final List<Object> arguments;

        private PositionalListArguments() {
            this(new ArrayList<>());
        }

        private PositionalListArguments(List<Object> arguments) {
            this.arguments = arguments;
        }

        public static Arguments ofUnmodifiable(List<?> arguments) {
            return new PositionalListArguments(Collections.unmodifiableList(arguments));
        }

        @Override public void add(Object value) {
            arguments.add(value);
        }

        @Override public void add(String key, Object value) {
            throw new IllegalStateException(MIXED_ARGS_NAMED + key);
        }

        @Override public Arguments getUnmodifiableSnapshot() {
            // `List.copyOf` is not used here as argument values can be `null`
            return ofUnmodifiable(new ArrayList<>(arguments));
        }

        @Override public Arguments getUnmodifiable() {
            return ofUnmodifiable(arguments);
        }

        @Override public int size() {
            return arguments.size();
        }

        @Override public List<Object> toList() {
            return arguments;
        }

        @Override public Map<String, Object> toNamedMap() {
            Map<String, Object> map = new HashMap<>();

            for (int i = 0; i < size(); i++) {
                map.put(intToString(i), arguments.get(i));
            }

            return Collections.unmodifiableMap(map);
        }

        @Override public Collection<Object> values() {
            return arguments;
        }

        @Override public Type type() {
            return Type.POSITIONAL;
        }

        @Override public Arguments resolve(Resolver resolver) {
            Objects.requireNonNull(resolver, NULL_RESOLVER_MESSAGE);
            List<Object> resolved = null;

            for (int i = 0; i < size(); i++) {
                Object originalValue = arguments.get(i);
                Object resolvedValue = resolver.resolve(i, originalValue);

                if (!Objects.equals(originalValue, resolvedValue)) {
                    if (resolved == null) {
                        resolved = new ArrayList<>(arguments);
                    }

                    resolved.set(i, resolvedValue);
                }
            }

            return resolved == null ? this : ofUnmodifiable(resolved);
        }
    }

    private static String intToString(int value) {
        return value < DIGITS.length ? DIGITS[value] : String.valueOf(value);
    }
}
