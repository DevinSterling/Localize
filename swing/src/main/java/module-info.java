/// # LocalizeSwing
/// Localize Swing integration module.
///
/// - Repository:
///   [https://github.com/DevinSterling/Localize](https://github.com/DevinSterling/Localize)
/// ___
/// Swing integration to automatically reflect changes in UI components
/// when the locale or arguments change using reactive-like bindings.
///
/// ### Mouse Clicker Example
/// Each time the `JButton` is clicked or the `JTextField` is edited,
/// the associated localized values are updated:
/// ```java
/// LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
/// localize.addProvider("messages");
///
/// AtomicInteger clickCount = new AtomicInteger();
/// JLabel clickDetails = new JLabel();
/// JButton clickButton = new JButton();
/// JTextField textField = new JTextField("Snowball");
///
/// clickButton.addActionListener(_ -> clickCount.getAndIncrement());
///
/// // Binding
/// localize.bind(clickButton, "MyApp.clickMe");
/// localize.get("MyApp.clickMessage")
///         .arg("click_count", clickCount::get)
///         .arg("name", textField)
///         .on(Trigger.action(clickButton))
///         .defaultValue("N/A")
///         .bind(clickDetails);
/// ```
/// @see com.devinsterling.localize.swing.LocalizeSwing
/// @since 2.0
module com.devinsterling.localize.swing {
    requires transitive com.devinsterling.localize;
    requires java.desktop;

    exports com.devinsterling.localize.swing;
}