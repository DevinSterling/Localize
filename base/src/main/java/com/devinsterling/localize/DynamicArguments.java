package com.devinsterling.localize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class DynamicArguments {
    private MutableArguments arguments;

    public void add(Object value) {
        if (arguments == null) {
            arguments = new Positional();
        }
        arguments.add(value);
    }

    public void add(String key, Object value) {
        if (arguments == null) {
            arguments = new Named();
        }
        arguments.add(key, value);
    }

    public Arguments snapshot() {
        return arguments == null ? Arguments.NONE : arguments.getUnmodifiableSnapshot();
    }

    public Arguments get() {
        return arguments == null ? Arguments.NONE : arguments.getUnmodifiable();
    }

    private interface MutableArguments extends Arguments {
        void add(Object value);
        void add(String key, Object value);
        Arguments getUnmodifiableSnapshot();
        Arguments getUnmodifiable();
    }

    public static final class Named implements MutableArguments {
        private final Map<String, Object> arguments;

        private Named() {
            this(new HashMap<>());
        }

        private Named(Map<String, Object> arguments) {
            this.arguments = arguments;
        }

        /// @return Unmodifiable wrapper.
        public static Arguments asUnmodifiable(Map<String, ?> arguments) {
            return new Named(Collections.unmodifiableMap(arguments));
        }

        @Override public void add(Object value) {
            throw new IllegalStateException(
                "Positional argument given; mixing named and positional arguments is not supported"
            );
        }

        @Override public void add(String key, Object value) {
            arguments.put(key, value);
        }

        @Override public Arguments getUnmodifiableSnapshot() {
            // `Map.copyOf` is not used here as argument values can be `null`
            return asUnmodifiable(new HashMap<>(arguments));
        }

        @Override public Arguments getUnmodifiable() {
            return asUnmodifiable(arguments);
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

        @Override public Type type() {
            return Type.NAMED;
        }

        @Override public Arguments resolve(Resolver resolver) {
            Objects.requireNonNull(resolver, "resolver must not be null");
            Map<String, Object> resolved = null;

            for (Map.Entry<String, Object> entry : arguments.entrySet()) {
                String key = entry.getKey();
                Object originalValue = entry.getValue();
                Object resolvedValue = resolver.resolve(key, originalValue);

                if (!Objects.equals(originalValue, resolvedValue)) {
                    if (resolved == null) {
                        resolved = new HashMap<>(arguments);
                    }

                    resolved.put(key, resolvedValue);
                }
            }

            return resolved == null ? this : asUnmodifiable(resolved);
        }
    }

    public static final class Positional implements MutableArguments {
        private static final String[] DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        private final List<Object> arguments;

        private Positional() {
            this(new ArrayList<>());
        }

        private Positional(List<Object> arguments) {
            this.arguments = arguments;
        }

        /// @return Unmodifiable wrapper.
        public static Arguments asUnmodifiable(List<?> arguments) {
            return new Positional(Collections.unmodifiableList(arguments));
        }

        @Override public void add(Object value) {
            arguments.add(value);
        }

        @Override public void add(String key, Object value) {
            throw new IllegalStateException(
                "Named argument given; mixing named and positional arguments is not supported: " + key
            );
        }

        @Override public Arguments getUnmodifiableSnapshot() {
            // `List.copyOf` is not used here as argument values can be `null`
            return asUnmodifiable(new ArrayList<>(arguments));
        }

        @Override public Arguments getUnmodifiable() {
            return asUnmodifiable(arguments);
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
                String key = i < DIGITS.length ? DIGITS[i] : String.valueOf(i);
                map.put(key, arguments.get(i));
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
            Objects.requireNonNull(resolver, "resolver must not be null");
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

            return resolved == null ? this : asUnmodifiable(resolved);
        }
    }
}
