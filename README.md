# Localize
A simple localization library.

[![Maven Central Version](https://img.shields.io/maven-central/v/com.devinsterling/localize-base?style=flat-square)](https://central.sonatype.com/artifact/com.devinsterling/localize-base)
[![Base Javadoc](https://javadoc.io/badge2/com.devinsterling/localize-base/javadoc.svg?style=flat-square)](https://javadoc.io/doc/com.devinsterling/localize-base)

___

Localize is a Java localization library that simplifies internationalizing applications. 
It’s built to be straightforward to set up and use.

1. Create a thread-safe `Localize` instance: 
   ```java
   Localize localize = Localize.of(Locale.ENGLISH);
   ```
2. Add a provider for resource bundles using a key:
   ```java
   localize.putBundleProvider("ProviderKey", locale -> ResourceBundle.getBundle("i18n.sample", locale));
   ``` 
3. Retrieve localized values by key:
   ```java
   String en = localize.getValue("MyProgram.clickButton"); // Returns "Click!"
   // Dynamically changing the locale
   localize.setLocale(Locale.JAPANESE);
   String ja = localize.getValue("MyProgram.clickButton"); // Returns "クリック！"
   ``` 
   
## Resource Bundles

A `Localize` instance accepts multiple providers as additional sources or fallbacks,
which can be removed dynamically.
Configuration can control scenarios such as where no value or when a resource bundle is not found.
```java
localize.putBundleProvider("Provider1", locale -> ResourceBundle.getBundle("i18n.sample", locale));
localize.putBundleProvider("Provider2", locale -> ResourceBundle.getBundle("i18n.other", locale));
...
// Removing a provider when no longer needed:
localize.removeBundleProvider("Provider1");
// If a value is not found in all providers, return the following instead:
localize.getConfig().setDefaultMissingValue("Missing value");
```

## Plurals and Arguments

Localize uses [ICU4J](https://unicode-org.github.io/icu/userguide/icu4j/) under the hood, 
though this can be changed by providing a custom 
[LocalizationRequestProcessor](base/src/main/java/com/devinsterling/localize/LocalizationRequestProcessor.java).
With the default processor, Localize supports both named and numbered arguments along with pluralization.

Here is a look inside the contents of a sample properties file:
```properties
MyApp.clickMessage = {name} clicked this button {click_count, plural,\
=0{zero times}\
=1{one time}\
other{# times}}.
MyApp.NumberArgs = {0} clicked this button {1, plural,\
=0{zero times}\
=1{one time}\
other{# times}}.
```
- Named Arguments:
  ```java
  localize.get("MyApp.clickMessage")
          .arg("click_count", 1)
          .arg("name", "John Doe")
          .value(); // Returns "John Doe clicked this button one time."
  ```
- Numbered Arguments:
  ```java
  localize.get("MyApp.NumberArgs")
          .arg("John Doe") // Argument 0
          .arg(55) // Argument 1
          .value(); // Returns "John Doe clicked this button 55 times."
  ```
## LocalizeFX — JavaFX Integration

[![JavaFX Javadoc](https://javadoc.io/badge2/com.devinsterling/localize-javafx/javadoc.svg?style=flat-square)](https://javadoc.io/doc/com.devinsterling/localize-javafx)
___

Through an integration module, Localize integrates with JavaFX observables to automatically 
reflect changes in UI components when the locale or observable arguments change without manual intervention.

### Mouse clicker example
Each time the Button is clicked or the TextField is edited, 
the associated localized values are updated.
```java
LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
localize.putBundleProvider("ProviderKey", locale -> ResourceBundle.getBundle("messages", locale));

DoubleProperty clickCount = new SimpleDoubleProperty();
Label clickDetails = new Label();
Button clickButton = new Button();
TextField textField = new TextField("Snowball");

clickButton.setOnAction(_ -> clickCount.set(clickCount.get() + 1));

// Binding
clickButton.textProperty().bind(localize.getBinding("MyApp.clickMe"));
label.textProperty().bind(localize.get("MyApp.clickMessage")
                                  .arg("click_count", clickCount)
                                  .arg("name", textField.textProperty())
                                  .binding());
```

___

## Usage with Maven and Gradle
- Dependency for base functionality:
  ```maven
  <dependency>
    <groupId>com.devinsterling</groupId>
    <artifactId>localize-base</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```
  ```gradle
  implementation("com.devinsterling:localize-base:1.0.0")
  ```
- Dependency for base functionality *and* JavaFX integration:
  ```maven
  <dependency>
    <groupId>com.devinsterling</groupId>
    <artifactId>localize-javafx</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```
  ```gradle
  implementation("com.devinsterling:localize-javafx:1.0.0")
  ```

## License
Apache 2.0 [license](LICENSE)