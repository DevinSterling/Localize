package com.devinsterling.localize.fx;

import com.devinsterling.localize.Localize;
import org.junit.jupiter.api.Test;

import javafx.beans.binding.StringBinding;

import java.util.Locale;

import static com.devinsterling.localize.fx.TestUtil.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaFXDispatchTest {

    static {
        startupJavaFXThread();
    }

    @Test void testLocaleChangeDispatchesToEdt() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        Localize.ProviderKey key = Localize.ProviderKey.of("key");

        localize.putBundleProvider(key, TEST_PROVIDER);
        StringBinding binding = localize.getBinding(TEST_KEY_CLICK_ME);

        localize.setLocale(Locale.KOREAN);
        localize.setLocale(Locale.JAPANESE);

        runOnJavaFXThreadAndWait(() -> assertEquals("クリック！", binding.get()));

        localize.putBundleProvider(key, TEST2_PROVIDER);

        runOnJavaFXThreadAndWait(() -> assertEquals("クリック！？", binding.get()));
    }
}
