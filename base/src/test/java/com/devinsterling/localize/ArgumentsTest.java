package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentsTest {

    @Test void testNoneArguments() {
        Arguments none = Arguments.of();

        assertSame(Arguments.NONE, none);
        assertEquals(0, none.size());
        assertEquals(Arguments.Type.NONE, none.type());
        assertTrue(none.isEmpty());
        assertFalse(none.isNamed());
        assertFalse(none.isPositional());
        assertEquals(Arguments.Type.NONE.hashCode(), none.hashCode());
        assertEquals("()", none.toString());

        assertTrue(none.values().isEmpty());
        assertEquals(Map.of(), none.toNamedMap());
        assertEquals(List.of(), none.toList());
        assertArrayEquals(new Object[] {}, none.toArray());
    }

    @Test void testNamedArguments() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Arguments named = Arguments.of(map);

        assertEquals(3, named.size());
        assertEquals(Arguments.Type.NAMED, named.type());
        assertTrue(named.isNamed());
        assertFalse(named.isPositional());
        assertFalse(named.isEmpty());
        assertEquals(Objects.hash(Arguments.Type.NAMED, map.hashCode()), named.hashCode());
        assertEquals("{a=1, b=2, c=3}", named.toString());

        assertSame(named.toNamedMap(), named.toNamedMap());
        assertNotSame(named.toList(), named.toList());

        assertEquals(map, named.toNamedMap());
        assertEquals(map.values().stream().toList(), named.toList());
        assertArrayEquals(map.values().toArray(), named.toArray());
    }

    @Test void testPositionalArguments() {
        Object[] array = { 1, 2, 3, 4 };
        Arguments positional = Arguments.of(array);

        assertEquals(4, positional.size());
        assertEquals(Arguments.Type.POSITIONAL, positional.type());
        assertTrue(positional.isPositional());
        assertFalse(positional.isNamed());
        assertFalse(positional.isEmpty());
        assertEquals(Objects.hash(Arguments.Type.POSITIONAL, Arrays.hashCode(array)), positional.hashCode());
        assertEquals("[1, 2, 3, 4]", positional.toString());

        assertSame(positional.toList(), positional.toList());
        assertNotSame(positional.toNamedMap(), positional.toNamedMap());

        List<Object> list = Arrays.asList(array);

        assertEquals(Map.of("0", 1, "1", 2, "2", 3, "3", 4), positional.toNamedMap());
        assertEquals(list, positional.toList());
        assertEquals(list, positional.values());
        assertArrayEquals(array, positional.toArray());
    }

    @Test void testPositionalNamedArguments() {
        // Positional named arguments do not have an entry point accessible from
        // the public `Arguments` factory methods; it is an internal detail.
        ArgumentsHelper helper = new ArgumentsHelper(Arguments.Type.NAMED);
        helper.add(1);
        helper.add("a");
        helper.add(3);
        Arguments positional = helper.snapshot();

        assertEquals(3, positional.size());
        assertEquals(Arguments.Type.POSITIONAL, positional.type());
        assertTrue(positional.isPositional());
        assertFalse(positional.isNamed());
        assertFalse(positional.isEmpty());
        assertEquals("[1, a, 3]", positional.toString());

        assertSame(positional.toNamedMap(), positional.toNamedMap());
        assertNotSame(positional.toList(), positional.toList());
    }

    @Test void testEmptyArguments() {
        Arguments emptyNamed = Arguments.of(Map.of());
        Arguments emptyPositional = Arguments.of(List.of());

        assertTrue(emptyNamed.isEmpty());
        assertTrue(emptyPositional.isEmpty());
        assertNotEquals(Arguments.NONE, emptyNamed);
        assertNotEquals(Arguments.NONE, emptyPositional);
        assertNotEquals(emptyNamed, emptyPositional);
    }

    @Test void testPositionalAndNamedArgumentsEquality() {
        List<Integer> positionalSource = List.of(1, 2, 3);
        Map<String, Integer> namedSource = Map.of("1", 1, "2", 2, "3", 3);

        Arguments positional = Arguments.of(positionalSource);
        Arguments named = Arguments.of(namedSource);

        assertEquals(positional, Arguments.of(new ArrayList<>(positionalSource)));
        assertEquals(named, Arguments.of(new LinkedHashMap<>(namedSource)));
        assertNotEquals(positional, named);
        assertNotEquals(Arguments.NONE, positional);
        assertNotEquals(Arguments.NONE, named);
    }

    @Test void testIndicesToString() {
        Object[] nums = new Object[20];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = i;
        }

        Arguments positional = Arguments.of(nums);
        Map<String, Object> map = positional.toNamedMap();

        for (int i = 0; i < nums.length; i++) {
            assertEquals(i, map.get(String.valueOf(i)));
        }
    }

    @Test void testResolution() {
        Arguments positional = Arguments.of(List.of(0, "!", 2));
        Arguments named = Arguments.of(Map.of("0", "!", "1", "!", "2", 2));
        Arguments empty = Arguments.of();

        assertSame(positional, positional.resolve(x -> x));
        assertSame(named, named.resolve(x -> x));
        assertSame(empty, empty.resolve(x -> x));

        Arguments resolvedPositional = positional.resolve(x -> x.equals("!") ? x : 1337);
        Arguments resolvedNamed = named.resolve(x -> !x.equals("!") ? x : 1337);

        assertNotSame(positional, resolvedPositional);
        assertNotSame(named, resolvedNamed);
        assertSame(empty, empty.resolve(x -> 1337));

        assertEquals(List.of(1337, "!", 1337), resolvedPositional.toList());
        assertEquals(Map.of("0", 1337, "1", 1337, "2", 2), resolvedNamed.toNamedMap());
    }

    @Test void testResolverReceivesIndexOrName() {
        List<Object> calls = new ArrayList<>();
        Arguments.Resolver recorder = new Arguments.Resolver() {
            @Override public Object resolve(Object value) {
                // This method never runs because the more specific methods that delegate to this one are overridden
                throw new AssertionError("This method must not be called");
            }

            @Override public Object resolve(int i, Object value) {
                calls.add(i);
                return value;
            }

            @Override public Object resolve(String key, Object value) {
                calls.add(key);
                return value;
            }
        };

        Arguments.of(List.of("a", "b")).resolve(recorder);
        assertEquals(List.of(0, 1), calls);

        calls.clear();
        Arguments.of(new LinkedHashMap<>(Map.of("x", 1))).resolve(recorder);
        assertEquals(List.of("x"), calls);

        calls.clear();
        ArgumentsHelper helper = new ArgumentsHelper(Arguments.Type.NAMED);
        helper.add("a");
        helper.add("b");
        helper.snapshot().resolve(recorder);
        assertEquals(List.of(0, 1), calls);
    }

    @Test void testSnapshotIsImmutable() {
        ArgumentsHelper none = new ArgumentsHelper(null);
        ArgumentsHelper positionalA = new ArgumentsHelper(null);
        ArgumentsHelper positionalB = new ArgumentsHelper(Arguments.Type.NAMED);
        ArgumentsHelper named = new ArgumentsHelper(null);

        positionalA.add("v");
        positionalB.add("v");
        named.add("k", "v");

        Arguments noneSnapshot = none.snapshot();
        Arguments positionalSnapshotA = positionalA.snapshot();
        Arguments positionalSnapshotB = positionalB.snapshot();
        Arguments namedSnapshot = named.snapshot();

        none.add("v2");
        positionalA.add("v2");
        positionalB.add("v2");
        named.add("k", "v2");

        assertEquals(Arguments.NONE, noneSnapshot);
        assertEquals(Arguments.of("v"), positionalSnapshotA);
        assertEquals(Arguments.of("v"), positionalSnapshotB);
        assertEquals(Arguments.of(Map.of("k", "v")), namedSnapshot);
    }
}
