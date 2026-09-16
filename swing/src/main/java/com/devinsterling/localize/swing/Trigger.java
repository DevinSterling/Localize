package com.devinsterling.localize.swing;

import com.devinsterling.localize.event.Subscription;

import javax.swing.AbstractButton;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/// An event emitter that runs subscribed tasks whenever a trigger occurs.
///
/// Triggers can range from [click events][action] to
/// [property change events][propertyChange(String, Component)].
///
/// @see SwingLocalizationValueBuilder#on(Trigger)
/// @since 2.0
@FunctionalInterface
public interface Trigger {
    /// Subscribes the given task to this trigger.
    ///
    /// @param task Task to run whenever the trigger occurs.
    /// @return     Subscription to cancel listening to the trigger, if needed.
    /// @throws NullPointerException If `task` is `null`.
    Subscription subscribe(Runnable task);

    /// Returns a [Trigger] that emits whenever the given component fires a
    /// [`PropertyChangeEvent`][java.beans.PropertyChangeEvent].
    ///
    /// @param component Component that emits property change events.
    /// @return          Subscribable trigger.
    /// @throws NullPointerException If `component` is `null`.
    static Trigger propertyChange(Component component) {
        return Trigger.of(
            Objects.requireNonNull(component, "component must not be null"),
            Component::addPropertyChangeListener,
            Component::removePropertyChangeListener,
            task -> (PropertyChangeListener) evt -> task.run()
        );
    }

    /// Returns a [Trigger] that emits whenever the given component fires a
    /// [`PropertyChangeEvent`][java.beans.PropertyChangeEvent] fires for the given property.
    ///
    /// @param propertyName Specific property to listen to.
    /// @param component    Component that emits property change events.
    /// @return             Subscribable trigger.
    /// @throws NullPointerException If `component` is `null`.
    static Trigger propertyChange(String propertyName, Component component) {
        Objects.requireNonNull(component, "component must not be null");

        return task -> {
            PropertyChangeListener listener = evt -> task.run();
            component.addPropertyChangeListener(propertyName, listener);
            return createSubscription(() -> component.removePropertyChangeListener(propertyName, listener));
        };
    }

    /// Returns a [Trigger] that emits whenever the given button
    /// ([`JButton`][javax.swing.JButton], [`JCheckBox`][javax.swing.JCheckBox], etc.) is clicked.
    ///
    /// @param button Button that emits click events.
    /// @return       Subscribable trigger.
    /// @throws NullPointerException If `button` is `null`.
    static Trigger action(AbstractButton button) {
        return Trigger.of(
            Objects.requireNonNull(button, "button must not be null"),
            AbstractButton::addActionListener,
            AbstractButton::removeActionListener,
            task -> e -> task.run()
        );
    }

    /// Returns a [Trigger] that emits whenever the given component's width or height changes.
    ///
    /// @param component Component that emits resize events.
    /// @return          Subscribable trigger.
    /// @throws NullPointerException If `component` is `null`.
    static Trigger resize(Component component) {
        return Trigger.of(
            Objects.requireNonNull(component, "component must not be null"),
            Component::addComponentListener,
            Component::removeComponentListener,
            task -> new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) {
                    task.run();
                }
            }
        );
    }

    /// Returns a [Trigger] that emits whenever the given list's selection changes.
    ///
    /// @param list List that emits selection events.
    /// @return     Subscribable trigger.
    /// @throws NullPointerException If `list` is `null`.
    static Trigger listSelection(JList<?> list) {
        return Trigger.of(
            Objects.requireNonNull(list, "list must not be null"),
            JList::addListSelectionListener,
            JList::removeListSelectionListener,
            task -> (ListSelectionListener) e -> {
                if (!e.getValueIsAdjusting()) {
                    task.run();
                }
            }
        );
    }

    /// Returns a [Trigger] that emits whenever the given table's row selection changes.
    ///
    /// @param table Table that emits row selection events.
    /// @return      Subscribable trigger.
    /// @throws NullPointerException If `table` is `null`.
    static Trigger tableRowSelection(JTable table) {
        Objects.requireNonNull(table, "table must not be null");

        return task -> {
            ListSelectionListener rowListener = e -> {
                if (!e.getValueIsAdjusting()) task.run();
            };

            PropertyChangeListener rowModelListener = e -> {
                ListSelectionModel oldModel = (ListSelectionModel) e.getOldValue();
                ListSelectionModel newModel = (ListSelectionModel) e.getNewValue();
                oldModel.removeListSelectionListener(rowListener);
                newModel.addListSelectionListener(rowListener);
                task.run();
            };

            table.addPropertyChangeListener("selectionModel", rowModelListener);
            table.getSelectionModel().addListSelectionListener(rowListener);

            return createSubscription(() -> {
                table.removePropertyChangeListener("selectionModel", rowModelListener);
                table.getSelectionModel().removeListSelectionListener(rowListener);
            });
        };
    }

    /// Returns a [Trigger] that emits whenever the given table's column selection changes.
    ///
    /// @param table Table that emits column selection events.
    /// @return      Subscribable trigger.
    /// @throws NullPointerException If `table` is `null`.
    static Trigger tableColumnSelection(JTable table) {
        Objects.requireNonNull(table, "table must not be null");

        return task -> {
            TableColumnModelListener columnListener = new TableColumnModelListener() {
                @Override public void columnAdded(TableColumnModelEvent e) {}

                @Override public void columnRemoved(TableColumnModelEvent e) {}

                @Override public void columnMoved(TableColumnModelEvent e) {}

                @Override public void columnMarginChanged(ChangeEvent e) {}

                @Override public void columnSelectionChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        task.run();
                    }
                }
            };

            PropertyChangeListener columnModelListener = e -> {
                TableColumnModel oldModel = (TableColumnModel) e.getOldValue();
                TableColumnModel newModel = (TableColumnModel) e.getNewValue();
                oldModel.removeColumnModelListener(columnListener);
                newModel.addColumnModelListener(columnListener);
                task.run();
            };

            table.addPropertyChangeListener("columnModel", columnModelListener);
            table.getColumnModel().addColumnModelListener(columnListener);

            return createSubscription(() -> {
                table.removePropertyChangeListener("columnModel", columnModelListener);
                table.getColumnModel().removeColumnModelListener(columnListener);
            });
        };
    }

    private static <T, Listener> Trigger of(
        T component,
        BiConsumer<T, Listener> addListener,
        BiConsumer<T, Listener> removeListener,
        Function<Runnable, Listener> listenerFactory
    ) {
        return task -> {
            Listener listener = listenerFactory.apply(task);
            addListener.accept(component, listener);
            return createSubscription(() -> removeListener.accept(component, listener));
        };
    }

    private static Subscription createSubscription(Runnable onDispose) {
        return new Subscription() {
            private boolean isActive = true;

            @Override public void dispose() {
                if (isActive) {
                    isActive = false;
                    onDispose.run();
                }
            }

            @Override public boolean isActive() {
                return isActive;
            }
        };
    }
}
