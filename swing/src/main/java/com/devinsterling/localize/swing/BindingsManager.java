package com.devinsterling.localize.swing;

import javax.swing.Timer;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/// All methods must be called on the swing thread; this class is not thread-safe.
final class BindingsManager implements BindingRegistry {
    private static final int DEBOUNCE_DELAY = 100;
    private final Set<Binding<?>> bindings = new LinkedHashSet<>();
    private final Timer cleanupTimer = new Timer(DEBOUNCE_DELAY, e -> cleanup());

    BindingsManager() {
        // By default, repeating is enabled which is not desired
        cleanupTimer.setRepeats(false);
    }

    @Override public void register(Binding<?> binding) {
        bindings.add(Objects.requireNonNull(binding, "binding must not be null"));
        requestCleanup();
    }

    @Override public void unregister(Binding<?> binding) {
        bindings.remove(Objects.requireNonNull(binding, "binding must not be null"));
        requestCleanup();
    }

    public void notifyListeners() {
        // NOTE: A snapshot is required here.
        // Calling `update` CAN be reentrant, such that it can add additional bindings.
        // For example, changing the text of a component can trigger a property change event in an application,
        // which could trigger creation of a new StringBinding.
        Binding<?>[] snapshot = bindings.toArray(new Binding<?>[0]);
        boolean needsCleanup = false;

        for (Binding<?> binding : snapshot) {
            if (binding.isActive()) {
                binding.update();
            } else {
                needsCleanup = true;
            }
        }

        if (needsCleanup) {
            cleanup();
        }
    }

    private void requestCleanup() {
        // Cleanup is delayed for bulk removals.
        if (!cleanupTimer.isRunning()) {
            // Calling restart is O(N), so this is preferred instead
            cleanupTimer.start();
        }
    }

    private void cleanup() {
        // INVARIANT NOTE: `dispose` is not called here as it's guaranteed to be an internal `WeakStringBinding`,
        // so if a binding is inactive, it is effectively already disposed.
        // In the future, if something other than WeakStringBinding's `weakComponent` must be disposed,
        // it must be updated to call `dispose` here and in `notifyListeners` whenever encountering inactive bindings.
        bindings.removeIf(binding -> !binding.isActive());
    }
}
