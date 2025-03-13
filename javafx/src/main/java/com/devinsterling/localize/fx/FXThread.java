package com.devinsterling.localize.fx;

import javafx.application.Platform;

final class FXThread {

    private FXThread() {}

    public static boolean isUIThread() {
        return Platform.isFxApplicationThread();
    }

    public static void onUIThread(Runnable runnable) {
        try {
            Platform.runLater(runnable);
        } catch (IllegalStateException ignore) {
            // If there is no UI thread,
            // run on the current thread instead.
            runnable.run();
        }
    }
}
