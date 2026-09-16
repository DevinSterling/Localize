package com.devinsterling.localize.fx;

import com.devinsterling.localize.ResourceBundleProvider;

import javafx.application.Platform;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TestUtil {
    public static final ResourceBundleProvider TEST_PROVIDER = locale -> ResourceBundle.getBundle("test", locale);
    public static final ResourceBundleProvider TEST2_PROVIDER = locale -> ResourceBundle.getBundle("test2", locale);
    public static final String TEST_KEY_CLICK_ME = "MyApp.clickMe";
    public static final String TEST_KEY_CLICK_LABEL = "MyApp.clickLabel";

    private TestUtil() {}

    public static LocalizeFX getLocalizeFXInstance() {
        LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
        localize.putProvider("key", TEST_PROVIDER);
        return localize;
    }

    public static void runOnJavaFXThreadAndWait(Runnable runnable) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            runnable.run();
            return null;
        });

        Platform.runLater(task);

        try {
            task.get();
        } catch (Throwable t) {
            // FutureTask wraps caught exceptions, so unwrap it to get the original exception
            sneakyThrow(t instanceof ExecutionException ? t.getCause() : t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    private static final AtomicBoolean javaFXIsStarted = new AtomicBoolean();
    public static void startupJavaFXThread() {
        if (javaFXIsStarted.compareAndSet(false, true)) {
            Platform.startup(() -> {});
        }
    }
}
