package com.devinsterling.localize.swing;

import com.devinsterling.localize.Arguments;
import com.devinsterling.localize.LocalizationRequest;
import com.devinsterling.localize.LocalizationRequestSource;
import com.devinsterling.localize.LocalizationValueBuilder;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolTip;
import javax.swing.text.JTextComponent;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/// Builder to provide a string binding of a formatted localized value.
///
/// ### Arguments
/// Components passed directly as arguments are weakly observed and listened to for any changes.
/// The following components are supported:
///
/// | Object           | Listened Property           |
/// |------------------|-----------------------------|
/// | [Frame]          | [Frame#getTitle]            |
/// | [Dialog]         | [Dialog#getTitle]           |
/// | [JLabel]         | [JLabel#getText]            |
/// | [JToolTip]       | [JToolTip#getTipText]       |
/// | [AbstractButton] | [AbstractButton#getText]    |
/// | [JTextComponent] | [JTextComponent#getText()]  |
/// | [JProgressBar]   | [JProgressBar#getValue]     |
/// | [JSlider]        | [JSlider#getValue]          |
/// | [JSpinner]       | [JSpinner#getValue]         |
/// | [JComboBox]      | [JComboBox#getSelectedItem] |
///
/// Passing components that are not listed above nor extends any of them,
/// are treated as plain objects with their string representation based on [Object#toString].
///
/// Passing arguments:
/// ```java
/// JProgressBar progress = new JProgressBar();
/// JTextField person = new JTextField();
///
/// localize.get("MyApp.progressMessage") // "Current progress for {name}: {value}%"
///         .arg("value", progress)       // Observes JProgressBar value changes
///         .arg("name", person)          // Observes JTextField text changes
///         .bind(progress);              // Binds formatted text to the JProgressBar string property
/// ```
///
/// ### Deferred Arguments
/// [SwingLocalizationValueBuilder] allows [Supplier]s as arguments.
/// To prevent memory leaks caused by suppliers holding strong references to components,
///
/// ```java
/// JFrame window = new JFrame();
///
/// // Recommended
/// localize.get("Window.status")               // "Window: x={x} y={y}"
///         .arg("x", window, Window::getX)     // Deferred x coordinate
///         .arg("y", window, Window::getY)     // Deferred y coordinate
///         .on(Trigger.move(window))           // todo
///         .bind(statusLabel);                 // Automatically pulls fresh layout values when evaluated
/// ```
///
/// @param <B> Builder instance type.
/// @since 2.0
public class SwingLocalizationValueBuilder<B extends SwingLocalizationValueBuilder<B>> extends LocalizationValueBuilder<B> {
    private SwingListenerBindingHelper listenerHelper;

    /// Creates a builder to request a specified localized value.
    ///
    /// @param source   Source to derive a formatted localized value from.
    /// @param localize Localization instance to handle requests.
    /// @throws NullPointerException if `source` or `localize` is `null`.
    protected SwingLocalizationValueBuilder(LocalizationRequestSource source, LocalizeSwing localize) {
        super(source, localize);
    }

    @Override protected Object interceptValue(Object value) {
        // NOTE: In Java 21, this can be converted into a switch
        if (value instanceof Frame frame) {
            listenerHelper().addPropertyListener("title", frame);
            value = new WeakSupplier<>(frame, Frame::getTitle);
        } else if (value instanceof Dialog dialog) {
            listenerHelper().addPropertyListener("title", dialog);
            value = new WeakSupplier<>(dialog, Dialog::getTitle);
        } else if (value instanceof JLabel label) {
            listenerHelper().addPropertyListener("text", label);
            value = new WeakSupplier<>(label, JLabel::getText);
        } else if (value instanceof AbstractButton button) {
            listenerHelper().addPropertyListener("text", button);
            value = new WeakSupplier<>(button, AbstractButton::getText);
        } else if (value instanceof JToolTip tip) {
            listenerHelper().addPropertyListener("tiptext", tip);
            value = new WeakSupplier<>(tip, JToolTip::getTipText);
        } else if (value instanceof JTextComponent text) {
            listenerHelper().addTextListener(text);
            value = new WeakSupplier<>(text, JTextComponent::getText);
        } else if (value instanceof JProgressBar bar) {
            listenerHelper().addChangeListener(bar, JProgressBar::addChangeListener, JProgressBar::removeChangeListener);
            value = new WeakSupplier<>(bar, JProgressBar::getValue);
        } else if (value instanceof JSlider slider) {
            listenerHelper().addChangeListener(slider, JSlider::addChangeListener, JSlider::removeChangeListener);
            value = new WeakSupplier<>(slider, JSlider::getValue);
        } else if (value instanceof JSpinner spinner) {
            listenerHelper().addChangeListener(spinner, JSpinner::addChangeListener, JSpinner::removeChangeListener);
            value = new WeakSupplier<>(spinner, JSpinner::getValue);
        } else if (value instanceof JComboBox<?> combo) {
            listenerHelper().addItemListener(combo, JComboBox::addItemListener, JComboBox::removeItemListener);
            value = new WeakSupplier<>(combo, JComboBox::getSelectedItem);
        }

        // Perform parent interception last; less precedence
        return super.interceptValue(value);
    }

    /// Adds a numbered *deferred* argument backed by a weak reference to the given component.
    ///
    /// The given function is evaluated each time the localized formatted value is computed.
    /// The component is referenced weakly so that it may be garbage collected when
    /// no longer in use.
    ///
    /// @param <T>       Type of the component.
    /// @param <U>       Type of the deferred value.
    /// @param component Component providing the value.
    /// @param getValue  Function to retrieve the current value from the component.
    /// @return          This builder instance.
    /// @throws NullPointerException If `component` or `getValue` is `null`.
    public <T, U> B arg(T component, Function<T, U> getValue) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(getValue, "getValue must not be null");
        return arg(new WeakSupplier<>(component, getValue));
    }

    /// Adds a named *deferred* argument backed by a weak reference to the given component.
    ///
    /// @param <T>       Type of the component.
    /// @param <U>       Type of the deferred value.
    /// @param key       Key to be inserted.
    /// @param component Component providing the value.
    /// @param getValue  Function to retrieve the current value from the component.
    /// @return          This builder instance.
    /// @throws NullPointerException If `key`, `component`, or `getValue` is `null`.
    public <T, U> B arg(String key, T component, Function<T, U> getValue) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(getValue, "getValue must not be null");
        return arg(key, new WeakSupplier<>(component, getValue));
    }


    /// Adds an event trigger that triggers bound components to recompute and update their text.
    ///
    /// Triggers are installed when methods such as [bind(Component)] are called.
    ///
    /// ### Example Usage
    /// ```
    /// JLabel label = ...;
    /// JButton clickButton = ...;
    /// AtomicInteger count = new AtomicInteger(0);
    ///
    /// localize.get("MyApp.clickCount")
    ///         .arg("count", count::get)
    ///         .on(Trigger.action(clickButton))
    ///         .bind(label);
    /// ```
    ///
    /// @param trigger Event trigger to add.
    /// @return        This builder instance.
    /// @throws NullPointerException If `trigger` is `null`.
    public B on(Trigger trigger) {
        Objects.requireNonNull(trigger, "trigger must not be null");
        listenerHelper().addTrigger(trigger);
        return getBuilder();
    }

    /// Binds the given component with all properties applied from this builder and returns a [StringBinding].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// The component's text is automatically updated when any of the passed observable arguments or the locale changes.
    /// The following components are supported through this method:
    ///
    /// | Component        | Bound Method             |
    /// |------------------|--------------------------|
    /// | [Frame]          | [Frame#setTitle]         |
    /// | [Dialog]         | [Dialog#setTitle]        |
    /// | [JLabel]         | [JLabel#setText]         |
    /// | [AbstractButton] | [AbstractButton#setText] |
    /// | [JTextComponent] | [JTextComponent#setText] |
    /// | [JProgressBar]   | [JProgressBar#setString] |
    /// | [JToolTip]       | [JToolTip#setTipText]    |
    ///
    /// Passing a component that is not listed above nor extends any of them,
    /// will throw an [IllegalArgumentException].
    /// For unsupported components, see [bind(Object, SetText)].
    ///
    /// ### Example Usage
    /// Binding to a [JLabel]:
    /// ```java
    /// JLabel label = new JLabel();
    ///
    /// localize.get("MyApp.message")
    ///         .arg("fooKey", "barValue")
    ///         .bind(label);
    /// ```
    ///
    /// Optionally, manually handling cleanup using the returned [StringBinding]:
    /// ```java
    /// JFrame window = new JFrame();
    ///
    /// // Using the returned binding is optional
    /// StringBinding binding = localize.get("MyApp.windowTitle").bind(window);
    ///
    /// // Manually breaking the binding
    /// // NOTE: Disposal happens automatically when the bound component is garbage collected.
    /// binding.dispose();
    /// ```
    /// @param component Component to bind.
    /// @return          Weak string binding. **(Usage is optional)**
    /// @throws NullPointerException If `component` is null.
    /// @throws IllegalArgumentException If `component` is an unsupported type.
    /// @see bindTooltip
    public StringBinding bind(Component component) {
        // NOTE: In Java 21, this can be converted into a switch
        if (component instanceof Frame frame) {
            return bind(frame, Frame::setTitle);
        } else if (component instanceof Dialog dialog) {
            return bind(dialog, Dialog::setTitle);
        } else if (component instanceof JLabel label) {
            return bind(label, JLabel::setText);
        } else if (component instanceof AbstractButton button) {
            return bind(button, AbstractButton::setText);
        } else if (component instanceof JToolTip tip) {
            return bind(tip, JToolTip::setTipText);
        } else if (component instanceof JProgressBar bar) {
            // If text is being bound to the progress bar, the string should be painted
            bar.setStringPainted(true);
            return bind(bar, JProgressBar::setString);
        } else if (component instanceof JTextComponent text) {
            StringBinding binding = bind(text, JTextComponent::setText);
            SwingListenerBindingHelper.addDocumentListener(text, binding);
            return binding;
        } else if (component == null) {
            throw new NullPointerException("component must not be null");
        }
        throw new IllegalArgumentException(
            "Unsupported component type: " + component.getClass() + " (Alternatively, use `bind("
            + component.getClass().getSimpleName() + ", "
            + component.getClass().getSimpleName() + "::setText)` instead)"
        );
    }

    /// Binds the given component's [tooltip][JComponent#setToolTipText] with all
    /// properties applied from this builder and returns a [StringBinding].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// The component's tooltip text is automatically updated when any
    /// of the passed observable arguments or the locale changes.
    ///
    /// @param component Component to bind.
    /// @return          Weak string binding. **(Usage is optional)**
    /// @throws NullPointerException If `component` is null.
    public StringBinding bindTooltip(JComponent component) {
        return bind(component, JComponent::setToolTipText);
    }

    /// Binds the given component using the given callback with all properties
    /// applied from this builder and returns a [StringBinding].
    ///
    /// **This method is intended to be called on the Swing UI (EDT) thread only.**
    ///
    /// ### Example Usage
    /// Binding to a custom component:
    /// ```java
    /// MessageComponent message = new MessageComponent();
    ///
    /// localize.get("MyApp.message")
    ///         .arg("fooKey", "barValue")
    ///         .bind(message, MessageComponent::setMessage);
    /// ```
    /// @param <T>            Type of the component.
    /// @param component      Component to bind.
    /// @param setterCallback Text setter callback; method reference.
    /// @return               Weak string binding. **(Usage is optional)**
    /// @throws NullPointerException If `component` or `setterCallback` is null.
    /// @see bind(Component)
    /// @see bindTooltip
    public <T> StringBinding bind(T component, SetText<T> setterCallback) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(setterCallback, "setter must not be null");
        LocalizeSwing localize = getLocalize();
        BindingRegistry registry = localize.getBindingRegistry();

        // Since Binding cannot be implemented as lambda, an anonymous class will implicitly capture this builder.
        // `SwingBinding` avoids that implicit capture:
        StringBinding binding = new WeakStringBinding<>(
            localize,
            getSource(),
            getDefaultValue(),
            snapshotArguments(),
            getResolver(),
            new WeakReference<>(component),
            setterCallback
        );

        if (listenerHelper != null) {
            listenerHelper.installListeners(binding);
        }

        registry.register(binding);
        binding.update();
        return binding;
    }

    protected LocalizeSwing getLocalize() {
        return (LocalizeSwing) super.getLocalize();
    }

    private SwingListenerBindingHelper listenerHelper() {
        return listenerHelper == null ? (listenerHelper = new SwingListenerBindingHelper()) : listenerHelper;
    }

    private static final class WeakStringBinding<T> implements StringBinding {
        private final LocalizeSwing localize;
        private final LocalizationRequestSource source;
        private final String defaultValue;
        private final Arguments arguments;
        private final Arguments.Resolver resolver;
        private final WeakReference<T> weakComponent;
        private final SetText<T> setter;
        private String text;
        private boolean disposed;

        WeakStringBinding(
            LocalizeSwing localize,
            LocalizationRequestSource source,
            String defaultValue,
            Arguments arguments,
            Arguments.Resolver resolver,
            WeakReference<T> weakComponent,
            SetText<T> setter
        ) {
            this.localize = localize;
            this.source = source;
            this.defaultValue = defaultValue;
            this.arguments = arguments;
            this.resolver = resolver;
            this.weakComponent = weakComponent;
            this.setter = setter;
        }

        @Override public void update() {
            T component = weakComponent.get();
            if (disposed || component == null) return;

            text = localize.formatValue(
                LocalizationRequest.Builder
                    .of(source)
                    .arguments(arguments.resolve(resolver))
                    .defaultValue(defaultValue)
                    .build()
            );

            setter.setText(component, text);
        }

        @Override public void dispose() {
            // INVARIANT NOTE: This method is never called within `BindingsManager`.
            // In the future, if something other than `weakComponent` must be disposed,
            // `BindingsManager` must be updated to call `dispose` whenever encountering inactive bindings.
            if (!disposed) {
                disposed = true;
                weakComponent.clear();
                localize.getBindingRegistry().unregister(this);
            }
        }

        @Override public boolean isActive() {
            return !disposed && weakComponent.get() != null;
        }

        @Override public String get() {
            return text;
        }
    }
}
