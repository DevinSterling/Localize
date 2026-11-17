package com.devinsterling.localize.swing;

import com.devinsterling.localize.event.Subscription;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

final class SwingListenerBindingHelper {
    private final List<SetupListener> setupListeners = new ArrayList<>();

    public void addPropertyListener(String property, Component component) {
        setupListeners.add(new SetupPropertyListener(property, component));
    }

    public void addTextListener(JTextComponent component) {
        setupListeners.add(new SetupTextListener(component));
    }

    public <T extends JComponent> void addChangeListener(
        T component,
        BiConsumer<T, ChangeListener> addChangeListener,
        BiConsumer<T, ChangeListener> removeChangeListener
    ) {
        setupListeners.add(new SetupChangeListener<>(component, addChangeListener, removeChangeListener));
    }

    public <T extends JComponent> void addItemListener(
        T component,
        BiConsumer<T, ItemListener> addItemListener,
        BiConsumer<T, ItemListener> removeItemListener
    ) {
        setupListeners.add(new SetupItemListener<>(component, addItemListener, removeItemListener));
    }

    public void addTrigger(Trigger trigger) {
        setupListeners.add(new SetupTriggerListener(trigger));
    }

    public void installListeners(Binding<?> binding) {
        // A weak wrapper avoids potentially leaking the
        // source binding on longer-lived components that are listened to
        WeakBinding weak = new WeakBinding(binding);

        for (SetupListener setupListener : setupListeners) {
            setupListener.setup(weak);
        }
    }

    public static void addDocumentListener(JTextComponent component, Binding<?> binding) {
        WeakBinding weak = new WeakBinding(binding);

        component.addPropertyChangeListener("document", new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (!weak.updateIfActive()) {
                    component.removePropertyChangeListener("document", this);
                }
            }
        });
    }

    private interface SetupListener {
        void setup(WeakBinding binding);
    }

    private record SetupPropertyListener(String property, Component component) implements SetupListener {
        @Override public void setup(WeakBinding binding) {
            component.addPropertyChangeListener(property, new PropertyChangeListener() {
                @Override public void propertyChange(PropertyChangeEvent evt) {
                    if (!binding.updateIfActive()) {
                        component.removePropertyChangeListener(property, this);
                    }
                }
            });
        }
    }

    private record SetupChangeListener<T extends JComponent>(
        T component,
        BiConsumer<T, ChangeListener> addChangeListener,
        BiConsumer<T, ChangeListener> removeChangeListener
    ) implements SetupListener {
        @Override public void setup(WeakBinding binding) {
            addChangeListener.accept(component, new ChangeListener() {
                @Override public void stateChanged(ChangeEvent e) {
                    if (!binding.updateIfActive()) {
                        removeChangeListener.accept(component, this);
                    }
                }
            });
        }
    }

    private record SetupItemListener<T extends JComponent>(
        T component,
        BiConsumer<T, ItemListener> addChangeListener,
        BiConsumer<T, ItemListener> removeChangeListener
    ) implements SetupListener {
        @Override public void setup(WeakBinding binding) {
            addChangeListener.accept(component, new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    if (!binding.updateIfActive()) {
                        removeChangeListener.accept(component, this);
                    }
                }
            });
        }
    }

    private record SetupTriggerListener(Trigger trigger) implements SetupListener {
        @Override public void setup(WeakBinding binding) {
            Subscription[] subscription = new Subscription[1];

            subscription[0] = trigger.subscribe(() -> {
                if (!binding.updateIfActive() && subscription[0] != null) {
                    subscription[0].dispose();
                }
            });
        }
    }

    private record SetupTextListener(JTextComponent component) implements SetupListener {
        @Override public void setup(WeakBinding binding) {
            DocumentListener listener = new DocumentListener() {
                private void update() {
                    if (!binding.updateIfActive()) {
                        component.getDocument().removeDocumentListener(this);
                    }
                }

                @Override public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override public void changedUpdate(DocumentEvent e) {
                    update();
                }
            };

            component.addPropertyChangeListener("document", new PropertyChangeListener() {
                @Override public void propertyChange(PropertyChangeEvent evt) {
                    // Swing guarantees Documents to not be `null`
                    Document oldDocument = (Document) evt.getOldValue();
                    Document newDocument = (Document) evt.getNewValue();
                    oldDocument.removeDocumentListener(listener);

                    if (binding.updateIfActive()) {
                        newDocument.addDocumentListener(listener);
                    } else {
                        component.removePropertyChangeListener("document", this);
                    }
                }
            });

            component.getDocument().addDocumentListener(listener);
        }
    }

    private static final class WeakBinding extends WeakReference<Binding<?>> {
        private WeakBinding(Binding<?> binding) {
            super(binding);
        }

        /// @return `false` if the binding is GC'd or inactive.
        private boolean updateIfActive() {
            Binding<?> binding = get();
            boolean isActive = binding != null && binding.isActive();

            if (isActive) {
                binding.update();
            }

            return isActive;
        }
    }
}
