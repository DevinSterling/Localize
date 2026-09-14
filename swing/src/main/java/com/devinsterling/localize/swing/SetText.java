package com.devinsterling.localize.swing;

/// A callback to set the text of a component.
///
/// ### Example Usage
/// ```java
/// SetText<JLabel> setter = JLabel::setText;
/// JLabel label = new JLabel();
///
/// setter.setText(label, "Aincrad");
///
/// assert label.getText().equals("Aincrad");
/// ```
/// @param <T> Component type to set the text of.
/// @see SwingLocalizationValueBuilder#bind(Object, SetText)
/// @since 2.0
@FunctionalInterface
public interface SetText<T> {
    /// Sets the given text on the given component.
    ///
    /// @param component Component to set text on.
    /// @param text      Text to set.
    void setText(T component, String text);
}
