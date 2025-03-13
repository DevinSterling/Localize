/// # LocalizeFX
/// Localize JavaFX integration Module
/// ___
/// LocalizeFX integrates with JavaFX observables to automatically
/// reflect changes in UI components when the locale or observable
/// arguments change without manual intervention.
///
/// ### Mouse clicker example
/// Each time the Button is clicked or the TextField is edited,
/// the associated localized values are updated.
/// ```java
/// LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
/// localize.putBundleProvider("ProviderKey", locale -> ResourceBundle.getBundle("messages", locale));
///
/// DoubleProperty clickCount = new SimpleDoubleProperty();
/// Label clickDetails = new Label();
/// Button clickButton = new Button();
/// TextField textField = new TextField("Snowball");
///
/// clickButton.setOnAction(_ -> clickCount.set(clickCount.get() + 1));
///
/// // Binding
/// clickButton.textProperty().bind(localize.getBinding("MyApp.clickMe"));
/// label.textProperty().bind(localize.get("MyApp.clickMessage")
///                                   .arg("click_count", clickCount)
///                                   .arg("name", textField.textProperty())
///                                   .binding());
/// ```
module com.devinsterling.localize.fx {
    requires transitive com.devinsterling.localize;
    requires javafx.graphics;

    exports com.devinsterling.localize.fx;
}