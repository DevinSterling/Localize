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
        for (SetupListener setupListener : setupListeners) {
            setupListener.setup(binding);
        }
    }

    public static void addDocumentListener(JTextComponent component, Binding<?> binding) {
        component.addPropertyChangeListener("document", new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (binding.isActive()) {
                    binding.update();
                } else {
                    component.removePropertyChangeListener(this);
                }
            }
        });
    }

    private interface SetupListener {
        void setup(Binding<?> binding);
    }

    private record SetupPropertyListener(String property, Component component) implements SetupListener {
        @Override public void setup(Binding<?> binding) {
            component.addPropertyChangeListener(property, new PropertyChangeListener() {
                @Override public void propertyChange(PropertyChangeEvent evt) {
                    if (binding.isActive()) {
                        binding.update();
                    } else {
                        component.removePropertyChangeListener(this);
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
        @Override public void setup(Binding<?> binding) {
            addChangeListener.accept(component, new ChangeListener() {
                @Override public void stateChanged(ChangeEvent e) {
                    if (binding.isActive()) {
                        binding.update();
                    } else  {
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
        @Override public void setup(Binding<?> binding) {
            addChangeListener.accept(component, new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    if (binding.isActive()) {
                        binding.update();
                    } else  {
                        removeChangeListener.accept(component, this);
                    }
                }
            });
        }
    }

    private record SetupTriggerListener(Trigger trigger) implements SetupListener {
        @Override public void setup(Binding<?> binding) {
            Subscription[] subscription = new Subscription[1];

            subscription[0] = trigger.subscribe(() -> {
                if (binding.isActive()) {
                    binding.update();
                } else if (subscription[0] != null) {
                    subscription[0].dispose();
                }
            });
        }
    }

    private record SetupTextListener(JTextComponent component) implements SetupListener {
        @Override public void setup(Binding<?> binding) {
            DocumentListener listener = new DocumentListener() {
                private void update() {
                    if (binding.isActive()) {
                        binding.update();
                    } else  {
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

                    if (binding.isActive()) {
                        newDocument.addDocumentListener(listener);
                        binding.update();
                    } else {
                        component.removePropertyChangeListener(this);
                    }
                }
            });

            component.getDocument().addDocumentListener(listener);
        }
    }
}
