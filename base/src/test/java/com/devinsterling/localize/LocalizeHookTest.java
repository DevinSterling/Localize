package com.devinsterling.localize;

import com.devinsterling.localize.event.LocaleChangeEvent;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static com.devinsterling.localize.TestUtil.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalizeHookTest {

    @Test void testOnLocaleChange() {
        AtomicInteger counter = new AtomicInteger(0);
        Localize localize = new Localize(Locale.ENGLISH, new LocalizeConfig()) {
            @Override protected void onLocaleChanged(LocaleChangeEvent change) {
                counter.incrementAndGet();
            }
        };

        assertEquals(0, counter.get());

        localize.setLocale(Locale.JAPANESE);
        assertEquals(1, counter.get());

        localize.setLocale(Locale.FRENCH);
        localize.setLocale(Locale.ENGLISH);
        assertEquals(3, counter.get());

        localize.setLocale(Locale.CHINESE);
        localize.setLocale(Locale.CHINESE);
        assertThrows(NullPointerException.class, () -> localize.setLocale(null));

        localize.setLocale(Locale.CHINESE);
        assertEquals(4, counter.get());
    }

    @Test void testOnProvidersChange() {
        AtomicInteger counter = new AtomicInteger(0);
        Localize localize = new Localize(Locale.ENGLISH, new LocalizeConfig()) {
            @Override protected void onProvidersChanged() {
                counter.incrementAndGet();
            }
        };

        assertEquals(0, counter.get());

        Localize.ProviderEntry entry1 = localize.addBundleProvider(TEST_PROVIDER_NAME);
        assertEquals(1, counter.get());

        localize.addBundleProvider(TEST_PROVIDER);
        localize.putBundleProvider("key", TEST2_PROVIDER);
        localize.putBundleProvider("key", TEST2_PROVIDER_NAME);
        localize.putBundleProvider(Localize.ProviderKey.of("key2"), TEST_PROVIDER);
        assertEquals(5, counter.get());

        localize.removeBundleProvider("non-existent key");
        localize.refresh("non-existent key");
        assertEquals(5, counter.get());

        localize.removeBundleProvider("key");
        localize.refresh("key2");
        assertEquals(7, counter.get());

        localize.refresh();
        assertEquals(8, counter.get());

        entry1.refresh();
        entry1.remove();
        assertEquals(10, counter.get());

        entry1.refresh();
        entry1.remove();
        assertEquals(10, counter.get());

        localize.clearBundleProviders();
        assertEquals(11, counter.get());

        localize.clearBundleProviders();
        assertEquals(11, counter.get());
    }
}
