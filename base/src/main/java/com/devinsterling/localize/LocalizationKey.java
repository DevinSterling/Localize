package com.devinsterling.localize;

/// Utility to pass an instance that implements this interface
/// directly to a [Localize] instance to request a value.
///
/// ### Example Usage:
/// ```
/// public enum MyMessageKeys implements LocalizationKey {
///     MESSAGE_BUTTON_HI("Message.button.hi"),
///     MESSAGE_BUTTON_WAVE("Message.button.wave"),
///     MESSAGE_LABEL_GREET("Message.label.greet");
///
///     private final String key;
///
///     private MyMessageKeys(String key) {
///         this.key = key;
///     }
///
///     @Override public getKey() {
///         return key;
///     }
/// }
/// ...
/// // Using the enum in action:
/// Button buttonHi = new Button();
/// buttonHi.setText(localize.getValue(MESSAGE_BUTTON_HI));
///
/// // With the JavaFX integration module for observable behavior:
/// buttonHi.textProperty().bind(localize.getBinding(MESSAGE_BUTTON_HI));
/// ```
/// @see Localize#getValue(LocalizationKey)
/// @since 1.0
public interface LocalizationKey {
    /// Get a resource bundle string key
    /// for retrieving an associated value.
    ///
    /// @return Resource bundle key.
    String getKey();
}
