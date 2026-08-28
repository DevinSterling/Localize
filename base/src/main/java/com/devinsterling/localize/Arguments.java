package com.devinsterling.localize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Arguments used to format localized messages.
///
/// Arguments are lightweight unmodifiable views over their backing data structures.
/// They are represented in two primary ways:
/// - **[Positional][isPositional]:** Sequential values accessed by index.
/// - **[Named][isNamed]:** Key-value pairs accessed by string keys.
///
/// To transform arguments before formatting, see [resolve].
///
/// ### Creation
/// Instances are created using the static factory methods listed here:
/// - [#of(Object...)] or [#of(List)] for positional arguments.
/// - [#of(Map)] for named arguments.
/// - [#of()] for no arguments ([neither named nor positional][Type#NONE]).
///
/// @see Type
/// @see Resolver
/// @since 2.0
public interface Arguments {
    /// Returns an empty [Arguments] instance that are [neither named nor positional][Type#NONE].
    ///
    /// @return Empty arguments.
    static Arguments of() {
        return Arguments.NONE;
    }

    /// Returns a [positional][isPositional] [Arguments] view of the given array.
    ///
    /// Changes to the array are reflected in the returned arguments.
    ///
    /// @param arguments Positional arguments.
    /// @return          Positional arguments view.
    /// @throws NullPointerException If `arguments` is `null`.
    static Arguments of(Object... arguments) {
        Objects.requireNonNull(arguments, "arguments must not be null");
        return of(Arrays.asList(arguments)); // zero-copy wrapper
    }

    /// Returns a [positional][isPositional] [Arguments] view of the given list.
    ///
    /// Changes to the list are reflected in the returned arguments.
    ///
    /// @param arguments Positional arguments.
    /// @return          Positional arguments view.
    /// @throws NullPointerException If `arguments` is `null`.
    static Arguments of(List<?> arguments) {
        Objects.requireNonNull(arguments, "arguments must not be null");
        return ArgumentsHelper.ofPositionalArguments(arguments);
    }

    /// Returns a [named][isNamed] [Arguments] view of the given map.
    ///
    /// Changes to the map are reflected in the returned arguments.
    ///
    /// @param arguments Named arguments.
    /// @return          Named arguments view.
    /// @throws NullPointerException If `arguments` is `null`.
    static Arguments of(Map<String, ?> arguments) {
        Objects.requireNonNull(arguments, "arguments must not be null");
        return ArgumentsHelper.ofNamedArguments(arguments);
    }

    /// Returns the total number of arguments.
    ///
    /// @return Number of arguments.
    int size();

    /// Returns `true` if there are no arguments (i.e., [size] is `0`).
    ///
    /// @return `true` if there are no arguments.
    default boolean isEmpty() {
        return size() == 0;
    }

    /// Returns the arguments as a list.
    ///
    /// ### [Named Arguments][isNamed]
    /// The order of named arguments is unspecified.
    ///
    /// @return Unmodifiable list of arguments.
    /// @see toArray
    /// @see toNamedMap
    default List<Object> toList() {
        // Implementors can override to return an unmodifiable view instead (e.g., DynamicArguments.Positional)
        // noinspection Java9CollectionFactory - `List.copyOf` is not used here as argument values can be `null`
        return Collections.unmodifiableList(new ArrayList<>(values()));
    }

    /// Returns the arguments as a newly allocated array.
    ///
    /// ### [Named Arguments][isNamed]
    /// The order of named arguments is unspecified.
    ///
    /// @return Newly allocated array of arguments.
    /// @see toList
    /// @see toNamedMap
    default Object[] toArray() {
        return values().toArray();
    }

    /// Returns the arguments as a string-keyed map.
    ///
    /// ### [Positional Arguments][isPositional]
    /// If the backing arguments are positional, each argument index is converted into its string form.
    /// For example, an argument at index `1` becomes a named argument with the key as `"1"`.
    ///
    /// @return Unmodifiable map of named arguments.
    /// @see toList
    /// @see resolve
    Map<String, Object> toNamedMap();

    /// Returns a view over argument values.
    ///
    /// This method is guaranteed to always be cheap to call by returning an unmodifiable view
    /// backed by this arguments instance, instead of allocating an entirely new collection.
    ///
    /// @return Unmodifiable view of argument values.
    Collection<Object> values();

    /// Returns the argument [type][Type] to determine if arguments
    /// are [positional][isPositional], [named][isNamed], or [neither][Type#NONE].
    ///
    /// @return Type of arguments.
    /// @see isNamed
    /// @see isPositional
    Type type();

    /// Transforms and returns the resolved arguments using the given resolver.
    ///
    /// If resolution produces no changes, this same instance is returned.
    /// Changes are identified by comparing the equality of each original and resolved value
    /// using [`Objects.equals(originalValue, resolvedValue)`][Objects#equals(Object, Object)].
    ///
    /// If resolution produces changes, an immutable snapshot containing the resolved arguments is returned.
    ///
    /// ### Example
    /// Resolving arguments to obfuscate text:
    /// ```
    /// Arguments resolved = arguments.resolve(value -> {
    ///     if (value instanceof PasswordField password) {
    ///         value = password.obfuscatedText();
    ///     }
    ///     return value;
    /// });
    /// ```
    /// @param resolver Resolver used to transform argument values.
    /// @return         This instance if resolution produces no changes. Otherwise, the immutable resolved arguments.
    /// @throws NullPointerException If `resolver` is `null`.
    Arguments resolve(Resolver resolver);

    /// Returns `true` if the arguments are [named][Type#NAMED].
    ///
    /// Named arguments consist of key-value pairs,
    /// where each key is a [String] and each value is an [Object].
    ///
    /// @return `true` if the arguments are named.
    /// @see toNamedMap
    default boolean isNamed() {
        return type() == Type.NAMED;
    }

    /// Returns `true` if the arguments are [positional][Type#POSITIONAL].
    ///
    /// Positional arguments consist of values in sequential order, like a list.
    ///
    /// @return `true` if the arguments are positional.
    /// @see toList
    /// @see toArray
    default boolean isPositional() {
        return type() == Type.POSITIONAL;
    }

    /// The type of [Arguments], identifying how arguments are represented.
    enum Type {
        /// No arguments, where the [Type] cannot be determined as either [NAMED] or [POSITIONAL].
        ///
        /// To prevent ambiguity when there are zero arguments without any context,
        /// they are treated as neither named nor positional.
        ///
        /// If the type is none, then [Arguments#isEmpty] is always `true`.
        NONE,

        /// Positional arguments (e.g., stored as sequential values).
        ///
        /// @see Arguments#isPositional
        POSITIONAL,

        /// Named arguments (e.g., stored as key-value pairs).
        ///
        /// Unlike [positional][POSITIONAL] arguments, order is unspecified.
        ///
        /// @see Arguments#isNamed
        NAMED,
    }

    /// Resolver to transform argument values based on the [value itself][resolve(Object)],
    /// [position][resolve(int, Object)], or [name][resolve(String, Object)].
    ///
    /// @implNote The default [positional][resolve(int, Object)] and
    ///           [named][resolve(String, Object)] methods delegate to [resolve(Object)].
    /// @see Arguments#resolve
    @FunctionalInterface
    interface Resolver {
        /// Resolves the given argument value.
        ///
        /// @param value Argument value to resolve.
        /// @return      Resolved argument value.
        /// @implSpec    If no resolution occurs, return the same value to denote no change.
        /// @see resolve(int, Object)
        /// @see resolve(String, Object)
        Object resolve(Object value);

        /// Resolves the given [positional][Type#POSITIONAL] argument value.
        ///
        /// @param index Argument index.
        /// @param value Argument value to resolve.
        /// @return      Resolved argument value.
        /// @implSpec    If no resolution occurs, return the same value to denote no change.
        /// @see resolve(Object)
        default Object resolve(int index, Object value) {
            return resolve(value);
        }

        /// Resolves the given [named][Type#NAMED] argument value.
        ///
        /// @param key   Argument key.
        /// @param value Argument value to resolve.
        /// @return      Resolved argument value.
        /// @implSpec    If no resolution occurs, return the same value to denote no change.
        /// @see resolve(Object)
        default Object resolve(String key, Object value) {
            return resolve(value);
        }
    }

    /// Empty arguments that are [neither named nor positional][Type#NONE].
    Arguments NONE = new Arguments() {
        private static final Object[] EMPTY_ARRAY = {};

        @Override public int size() {
            return 0;
        }

        @Override public Object[] toArray() {
            return EMPTY_ARRAY;
        }

        @Override public List<Object> toList() {
            return List.of();
        }

        @Override public Map<String, Object> toNamedMap() {
            return Map.of();
        }

        @Override public Collection<Object> values() {
            return List.of();
        }

        @Override public Type type() {
            return Type.NONE;
        }

        @Override public Arguments resolve(Resolver resolver) {
            // The interface requires `resolver` to be non-null despite it not being used here
            Objects.requireNonNull(resolver, "resolver must not be null");
            return this;
        }
    };
}
