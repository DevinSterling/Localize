/// # LocalizeFX Example
///
/// Sample usage of Localize with JavaFX and ICU4J integration.
module com.devinsterling.localize.example {
    requires com.devinsterling.localize.fx;
    requires javafx.controls;

    exports com.devinsterling.localize.example to javafx.graphics;
}