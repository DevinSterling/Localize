package com.devinsterling.localize;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

        assertEquals(Map.of(), none.toNamedMap());
        assertEquals(List.of(), none.toList());
        assertArrayEquals(new Object[] {}, none.toArray());
    }

    @Test void testNamedArguments() {
        Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);
        Arguments named = Arguments.of(map);

        assertEquals(3, named.size());
        assertEquals(Arguments.Type.NAMED, named.type());
        assertTrue(named.isNamed());
        assertFalse(named.isPositional());
        assertFalse(named.isEmpty());

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

        List<Object> list = Arrays.asList(array);

        assertEquals(Map.of("0", 1, "1", 2, "2", 3, "3", 4), positional.toNamedMap());
        assertEquals(list, positional.toList());
        assertEquals(list, positional.values());
        assertArrayEquals(array, positional.toArray());
    }

    @Test void testEmptyArguments() {
        Arguments emptyNamed = Arguments.of(Map.of());
        Arguments emptyPositional = Arguments.of(List.of());

        assertTrue(emptyNamed.isEmpty());
        assertTrue(emptyPositional.isEmpty());
    }

    @Test void testIndicesToString() {
        Object[] nums = new Object[20];
        Arguments positional = Arguments.of(nums);

        for (int i = 0; i < nums.length; i++) {
            nums[i] = i;
        }

        Map<String, Object> map = positional.toNamedMap();

        for (int i = 0; i < nums.length; i++) {
            assertEquals(i, map.get(String.valueOf(i)));
        }
    }

    @Test void testResolution() {
        Arguments positional = Arguments.of(List.of("!", 2));
        Arguments named = Arguments.of(Map.of("1", "!", "2", 2));
        Arguments empty = Arguments.of();

        assertSame(positional, positional.resolve(x -> x));
        assertSame(named, named.resolve(x -> x));
        assertSame(empty, empty.resolve(x -> x));

        Arguments resolvedPositional = positional.resolve(x -> x == "!" ? x : 1337);
        Arguments resolvedNamed = named.resolve(x -> x != "!" ? x : 1337);

        assertNotSame(positional, resolvedPositional);
        assertNotSame(named, resolvedNamed);
        assertSame(empty, empty.resolve(x -> 1337));

        assertEquals(List.of("!", 1337), resolvedPositional.toList());
        assertEquals(Map.of("1", 1337, "2", 2), resolvedNamed.toNamedMap());
    }
}
