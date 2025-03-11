/// # Localize
/// A simple localization library.
/// ___
/// Localize is a Java localization library that simplifies internationalizing applications.
/// It’s built to be straightforward to set up and use.
///
/// 1. Create a `Localize` instance:
///    ```java
///    Localize localize = Localize.of(Locale.ENGLISH);
///    ```
/// 2. Add a provider for resource bundles using a key:
///    ```java
///    localize.putBundleProvider("ProviderKey", locale -> ResourceBundle.getBundle("i18n.sample", locale));
///    ```
/// 3. Retrieve localized values by key:
///    ```java
///    String en = localize.getValue("MyProgram.clickButton"); // Returns "Click!"
///    // Dynamically changing the locale
///    localize.setLocale(Locale.JAPANESE);
///    String ja = localize.getValue("MyProgram.clickButton"); // Returns "クリック！"
///    ```
///
/// ## Resource Bundles
///
/// A `Localize` instance accepts multiple providers as additional sources or fallbacks,
/// which can be removed dynamically.
/// Configuration can control scenarios such as where no value or when a resource bundle is not found.
/// ```java
/// localize.putBundleProvider("Provider1", locale -> ResourceBundle.getBundle("i18n.sample", locale));
/// localize.putBundleProvider("Provider2", locale -> ResourceBundle.getBundle("i18n.other", locale));
/// ...
/// // Removing a provider when no longer needed:
/// localize.removeBundleProvider("Provider1");
/// // If a value is not found in all providers, return the following instead:
/// localize.getConfig().setDefaultMissingValue("Missing value");
/// ```
///
/// ## Plurals and Arguments
///
/// Localize uses [ICU4J](https://unicode-org.github.io/icu/userguide/icu4j/) under the hood,
/// though this can be changed by providing a custom `LocalizationRequestProvider`.
/// With the default provider, Localize supports both named and numbered arguments along with pluralization.
///
/// Here is a look inside the contents of a sample properties file:
/// ```properties
/// MyApp.clickMessage = {name} clicked this button {click_count, plural,\
/// =0{zero times}\
/// =1{one time}\
/// other{# times}}.
/// MyApp.NumberArgs = {0} clicked this button {1, plural,\
/// =0{zero times}\
/// =1{one time}\
/// other{# times}}.
/// ```
/// - Named Arguments:
///   ```java
///   localize.get("MyApp.clickMessage")
///           .arg("click_count", 1)
///           .arg("name", "John Doe")
///           .value(); // Returns "John Doe clicked this button one time."
///   ```
/// - Numbered Arguments:
///   ```java
///   localize.get("MyApp.NumberArgs")
///           .arg("John Doe") // Argument 0
///           .arg(55) // Argument 1
///           .value(); // Returns "John Doe clicked this button 55 times."
///   ```
/// ___
///
/// Localize also features an integration module for JavaFX, **LocalizeFX**,
/// to automatically reflect changes in the UI.
module com.devinsterling.localize {
    requires com.ibm.icu;
    requires org.slf4j;

    exports com.devinsterling.localize;
}