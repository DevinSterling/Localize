# Localize
A simple-to-use Java/FX localization library (Supports Java 17+).

[![Maven Central Version](https://img.shields.io/maven-central/v/com.devinsterling/localize-base?style=flat-square)](https://central.sonatype.com/artifact/com.devinsterling/localize-base)
[![Base Javadoc](https://javadoc.io/badge2/com.devinsterling/localize-base/javadoc.svg?logo=&style=flat-square)](https://javadoc.io/doc/com.devinsterling/localize-base)

## Usage with Maven and Gradle
- Base functionality only:
  ```xml
  <dependency>
    <groupId>com.devinsterling</groupId>
    <artifactId>localize-base</artifactId>
    <version>2.0.0</version>
  </dependency>
  ```
  ```kts
  implementation("com.devinsterling:localize-base:2.0.0")
  ```

- [Base functionality + JavaFX integration](#localizefx--javafx-integration)
- [Base functionality + Swing integration](#localizeswing--swing-integration)
- [ICU4J integration](#icu4j-integration)

___

Localize is a Java localization library that simplifies internationalizing applications. 
It’s designed to be straightforward to set up and use.

1. Create a thread-safe `Localize` instance: 
   ```java
   Localize localize = Localize.of(Locale.ENGLISH);
   ```
2. Add a provider for resource bundles:
   ```java
   // Insert using a unique key
   localize.putBundleProvider("ProviderKey", locale -> ResourceBundle.getBundle("i18n.sample", locale));
   // Or without
   localize.addBundleProvider(locale -> ResourceBundle.getBundle("i18n.sample", locale));
   // Or by resource bundle base name
   localize.addBundleProvider("i18n.sample");
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
localize.addBundleProvider("i18n.other");
...

// Removing a provider when no longer needed:
localize.removeBundleProvider("Provider1");

// If a value is not found in any provider, configure a global fallback:
localize.getConfig().setDefaultMissingValue("Missing value");
```

## Plurals and Arguments

> For advanced message formatting, plural rules, and greater control over bundle properties,
see the [ICU4J integration module](#icu4j-integration).

By default, Localize uses a
[LocalizationFormatter](https://javadoc.io/doc/com.devinsterling/localize-base/latest/com.devinsterling.localize/com/devinsterling/localize/LocalizationFormatter.html)
built around Java's
[MessageFormat](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html),
though this can be replaced
[programmatically](https://javadoc.io/doc/com.devinsterling/localize-base/latest/com.devinsterling.localize/com/devinsterling/localize/Localize.html#setFormatter(com.devinsterling.localize.LocalizationFormatter))
or via SPI by providing a custom formatter.
The default formatter supports both named and numbered arguments,
as well as pluralization through
[ChoiceFormat](https://docs.oracle.com/javase/8/docs/api/java/text/ChoiceFormat.html)
choice patterns.

Here is a look inside the contents of a sample properties file:
```properties
MyApp.numberedArguments={0} clicked this button {1, choice,\
0 #zero times|\
1 #one time|\
1 <{1} times}!
MyApp.clickMessage={name} clicked this button {click_count, choice,\
0 #zero times|\
1 #one time|\
1 <{click_count} times}!
```
- Numbered Arguments:
  ```java
  localize.get("MyApp.numberedArguments")
          .arg("John Doe") // Argument 0
          .arg(55) // Argument 1
          .value(); // Returns "John Doe clicked this button 55 times!"
  ```
- Named Arguments:
  ```java
  localize.get("MyApp.clickMessage")
          .arg("click_count", 1)
          .arg("name", "John Doe")
          .value(); // Returns "John Doe clicked this button one time!"
  ```
- Direct Formatting with Named Arguments:
  ```java
  localize.format("{name}, you've clicked {count} times!")
          .arg("name", "Jane Doe")
          .arg("count", 1337)
          .value(); // Returns "Jane Doe, you've clicked 1,337 times!"
  ```
---

## LocalizeFX — JavaFX Integration

[![LocalizeFX Javadoc](https://javadoc.io/badge2/com.devinsterling/localize-javafx/javadoc.svg?style=flat-square)](https://javadoc.io/doc/com.devinsterling/localize-javafx)

The JavaFX integration module is self-contained and includes all base functionality:
```xml
<dependency>
  <groupId>com.devinsterling</groupId>
  <artifactId>localize-javafx</artifactId>
  <version>2.0.0</version>
</dependency>
```
```kts
implementation("com.devinsterling:localize-javafx:2.0.0")
```

JavaFX integration to automatically reflect changes in UI components
when the locale or observable arguments change without manual intervention.

### Mouse Clicker Example
Each time the `Button` is clicked or the `TextField` is edited, 
the associated localized values are updated:
```java
LocalizeFX localize = LocalizeFX.of(Locale.ENGLISH);
localize.addBundleProvider("messages");

DoubleProperty clickCount = new SimpleDoubleProperty();
Label clickDetails = new Label();
Button clickButton = new Button();
TextField textField = new TextField("Snowball");

clickButton.setOnAction(_ -> clickCount.set(clickCount.get() + 1));

// Binding
clickButton.textProperty().bind(localize.getBinding("MyApp.clickMe"));
clickDetails.textProperty().bind(localize.get("MyApp.clickMessage")
                                  .arg("click_count", clickCount)
                                  .arg("name", textField.textProperty())
                                  .defaultValue("N/A")
                                  .binding());
```

---

## LocalizeSwing — Swing Integration

[![LocalizeSwing Javadoc](https://javadoc.io/badge2/com.devinsterling/localize-swing/javadoc.svg?style=flat-square)](https://javadoc.io/doc/com.devinsterling/localize-swing)

The Swing integration module is self-contained and includes all base functionality:
```xml
<dependency>
  <groupId>com.devinsterling</groupId>
  <artifactId>localize-swing</artifactId>
  <version>2.0.0</version>
</dependency>
```
```kts
implementation("com.devinsterling:localize-swing:2.0.0")
```

Swing integration to automatically reflect changes in UI components
when the locale or arguments change using reactive-like bindings.

### Mouse Clicker Example
Each time the `JButton` is clicked or the `JTextField` is edited,
the associated localized values are updated:
```java
LocalizeSwing localize = LocalizeSwing.of(Locale.ENGLISH);
localize.addBundleProvider("messages");

AtomicInteger clickCount = new AtomicInteger();
JLabel clickDetails = new JLabel();
JButton clickButton = new JButton();
JTextField textField = new JTextField("Snowball");

clickButton.addActionListener(_ -> clickCount.getAndIncrement());

// Binding
localize.bind(clickButton, "MyApp.clickMe");
localize.get("MyApp.clickMessage")
        .arg("click_count", clickCount::get)
        .arg("name", textField)
        .on(Trigger.action(clickButton))
        .defaultValue("N/A")
        .bind(clickDetails);
```

---

## ICU4J Integration

[![Localize ICU4J Javadoc](https://javadoc.io/badge2/com.devinsterling/localize-icu4j/javadoc.svg?style=flat-square)](https://javadoc.io/doc/com.devinsterling/localize-icu4j)

The ICU4J integration module requires at least one of the
[concrete Localize modules listed above](#usage-with-maven-and-gradle) (e.g., `localize-base`, `localize-javafx`):
```xml
<dependency>
  <groupId>com.devinsterling</groupId>
  <artifactId>localize-icu4j</artifactId>
  <version>2.0.0</version>
</dependency>
```
```kts
implementation("com.devinsterling:localize-icu4j:2.0.0")
```

### Usage

The integration module automatically registers the default
[LocalizationFormatter](https://javadoc.io/doc/com.devinsterling/localize-base/latest/com.devinsterling.localize/com/devinsterling/localize/LocalizationFormatter.html)
via SPI, so no code changes are required.

ICU4J's
[MessageFormat v2](https://messageformat.unicode.org/docs/quick-start/)
is used as the default message formatter, enabling ICU MessageFormat features
such as advanced syntax, variables, functions, and pluralization:
```properties
MyApp.clickMessage=.input {$click_count :number} \
.match $click_count \
0 {{{$name} did not click this button!}} \
1 {{{$name} clicked this button one time!}} \
* {{{$name} clicked this button {$click_count} times!}}
```

The default ICU4J formatter can be configured within `localize.properties` under `resources`:
```properties
# Prefer the original message syntax
localize.icu4j.formatter=com.ibm.icu.text.MessageFormat
```

Alternatively, the ICU4J formatter can be configured as a JVM system property,
which takes precedence over the properties file:
```
-Dlocalize.icu4j.formatter=com.ibm.icu.text.MessageFormat
```

## License
Apache 2.0 [license](LICENSE)