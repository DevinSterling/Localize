# Changelog

## 2.0.0 (2026-09-30)
Localize 2.0 is a major release, which now features a lightweight zero-dependency core module.
This release introduces a Swing integration module, decouples ICU4J support into an optional SPI module,
overhauls `Localize` instances with an event system and greater lower-level control,
and enables domain-specific instances with shared state.

### Additions　**＋**
- New integration modules:
  - `localize-icu4j`: ICU4J integration via SPI for Unicode MessageFormat syntax.
  - `localize-swing`: Swing integration with reactive component bindings.
- New examples:
  - `examples/javafx-icu4j-example`: Demonstrates JavaFX + ICU4J integration.
  - `examples/swing-example`: Demonstrates Swing integration.

#### Core Module (`localize-base`)
- New core packages:
  - `com.devinsterling.localize.event`: Event handling,
    including `LocalizeEvent`, `Subscription`, `EventListener`, `LocaleEvent`, and more.
  - `com.devinsterling.localize.spi`: Service Provider Interface (`LocalizationFormatterProvider`)
    for custom default formatters.

- New methods for `LocalizeConfig`:
  - `setMissingValueHandler`/`getMissingValueHandler`
  - `setIgnoreListenerExceptions`/`isIgnoreListenerExceptions`

- New constructor for `Localize`:
  - `Localize(Localize)`: Shares internal state with an existing instance.

- New methods for `Localize`:
  - `addListener`: Registers an event listener.
  - `removeListener`: Deregisters an event listener.
  - `putProvider(ProviderKey, String)`/`addProvider(String)`:
    Shorthand to add a provider using a string `ResourceBundle` base name.
  - `containsProvider`: Checks whether a provider exists.
  - `clearProviders`: Removes all providers.
  - `getProviderEntry`: View a specific provider.
  - `getProviderEntries`: Live unmodifiable view of all providers.
  - `format`: Formats a given pattern.
  - `formatValue(LocalizationRequest)`: Formats a request directly.
  - *protected* `fireEvent`: Fires an event to all attached instances and listeners.
  - *protected* `onEvent`: Hook into all events.
  - *protected* `onProviderEvent`: Hook into provider-scoped events.
  - *protected* `onLocaleReplaced`: Hook into locale changes.
  - *protected* `onFormatterReplaced`: Hook into formatter changes.

- Expose the nested class `Localize.ProviderEntry` alongside the `ProviderKey` interface.

- New methods for `LocalizationValueBuilder`:
  - `arg(Supplier)`/`arg(String, Supplier)`: Conveniently add deferred arguments.
  - `arg(T, Function<T, U>)`/`arg(String, T, Function<T, U>)`: Conveniently add weakly deferred arguments.
  - `defaultHandler`: Sets the handler to provide a default value when no value is found for a specified key.
  - *protected* `interceptValue`: Intercept argument values before they are stored.
  - *protected* `snapshotArguments`: Immutable snapshot of the current arguments.
  - *protected* `getResolver`: Resolver used for arguments resolution.
  - *protected* `getLocalize`: `Localize` instance the builder originated from.
  - *protected* `getSource`: Request source (i.e., resource key or format pattern).
  - *protected* `getMissingValueHandler`: Handler providing default values.

- New method for `LocalizationFormatter`:
  - `argumentsHint`: Optimizes how arguments are stored.

- New interfaces in `com.devinsterling.localize`:
  - `Arguments`: Abstraction to enhance arguments flexibility, replacing `Map<String, Object>`.
  - `LocalizationRequestSource`: Identifies the source of a localization request (`Key` or `Pattern`).
  - `MissingValueHandler`: Provides a default value when no value is found for a specified key.

- New class in `com.devinsterling.localize`:
  - `DefaultArgumentsResolver`: Implementation of `Arguments.Resolver` to handle deferred arguments.

#### JavaFX Module (`localize-javafx`)
- New constructor `LocalizeFX(Localize)` and *static* method `LocalizeFX#attach(Localize)`
  to share internal state with an existing instance.

- New classes in `com.devinsterling.localize.fx`:
  - `FXArgumentsResolver`: Implementation of `Arguments.Resolver` to handle resolution of JavaFX observable values.

#### ICU4J (`localize-icu4j`) and Swing (`localize-swing`) Modules
As new modules introduced in 2.0, see their documentation for more info:
- https://javadoc.io/doc/com.devinsterling/localize-icu4j
- https://javadoc.io/doc/com.devinsterling/localize-swing

### Fixes　**✓**
- Fix `LocalizeConfig#equals` to use `Objects.equals` instead of calling
  equals directly on a potentially null `defaultMissingValue` field.

### Breaking Changes　**⟳**
- The core module (`localize-base`) now has zero external dependencies.
  ICU4J support is now optional via the `localize-icu4j` integration module.
- Missing values now default to `[key]` instead of an empty string, making unresolved resource keys visible by default.
- `LocalizationFormatter#format` now accepts a single argument instead (`LocalizationFormatter.Request`).
- Change visibility from protected to private for `Localize#refresh(Locale)`.
- Update `LocalizationValueBuilder` constructor to `(LocalizationRequestSource, Localize)`.
- Update `FXLocalizationValueBuilder` constructor to `(LocalizationRequestSource, LocalizeFX)`.
- Rename `Localize#getProcessor` to `#getFormatter`.
- Rename `Localize#setProcessor` to `#setFormatter`.
- Rename `Localize#putBundleProvider` to `#putProvider`.
- Rename `Localize#addBundleProvider` to `#addProvider`.
- Rename `Localize#removeBundleProvider` to `#removeProvider`.
- Rename `Localize#refresh(String)` to `#refreshProvider(String)`.
- Rename `Localize#refresh()` to `#refreshProviders()`.
- Rename `Localize#applyBuilderProperties` to `#formatValue`, and change visibility from protected to public.
- Rename `LocalizeConfig#setThrowWhenNoValueFound` to `#setThrowOnMissingValue`.
- Rename `LocalizeConfig#setIgnoreProcessingExceptions` to `#setIgnoreFormatterExceptions`.
- Rename `LocalizeConfig#setIgnoreMissingResourceBundles` to `#setIgnoreProviderExceptions`.
- Rename `LocalizeConfig#isThrowWhenNoValueFound` to `#isThrowOnMissingValue`.
- Rename `LocalizeConfig#isIgnoreProcessingExceptions` to `#isIgnoreFormatterExceptions`.
- Rename `LocalizeConfig#isIgnoreMissingResourceBundles` to `#isIgnoreProviderExceptions`.
- Rename `LocalizationRequestProcessor` to `LocalizationFormatter`.
- Rename `LocalizationFormatter#process` to `#format`.
- Remove `LocalizationRequest#hasArguments` to avoid redundant API calls (Prefer `#getArguments`).
- Replace `LocalizeConfig#getDefaultMissingValue` with `#getMissingValueHandler`.
- Replace `LocalizationValueBuilder#getDefaultValue` with `#getMissingValueHandler`.
- Replace `LocalizationRequest#getDefaultValue` with `#getMissingValueHandler`.
- Replace `LocalizationRequest.Builder#defaultValue` with `#missingValueHandler`.

## 1.3.0 (2026-08-03)
### Additions　**＋**
- New `Localize#addBundleProvider` method to add a `ResourceBundleProvider` without explicitly specifying a key.

### Changes　**⟳**
- Improve concurrency and thread-safety of `Localize` implementations.
- Remove lock contention during `ResourceBundle` loading.
- Refine documentation for enhanced clarity.

### Fixes　**✓**
- Fix inconsistent provider state when adding or removing providers concurrently.
- Prevent stale `ResourceBundle` updates from overwriting newer locale changes.
- Ensure `LocalizeFX#getLocale` consistently reflects the current locale across all threads.

## 1.2.0 (2026-04-30)
### Additions　**＋**
Add new convenience static factory methods:
- `Localize#of(LocalizeConfig)`
- `LocalizeFX#of(LocalizeConfig)`

### Fixes　**✓**
- Ensure locale changes propagate to string bindings when the property
  is in an invalid state (e.g., in non-JavaFX environments).
- Synchronize `putBundleProvider` and `setLocale` to prevent stale resource bundles
  when the locale changes while simultaneously adding new providers.

### Changes　**⟳**
- Refine documentation for enhanced clarity.
- Improve ergonomics by implicitly notifying listeners whenever a resource bundle provider is inserted or removed.
  `refresh` methods are now intended for reloading bundles (e.g., from disk) at runtime.

## 1.1.0 (2025-07-09)
### Additions　**＋**
- New `LocalizationValueBuilder#defaultValue` method to specify a default value
  to return when a given key is not found.
- `LocalizationRequest` now features an inner static `Builder` class to build requests 
  more ergonomically.

### Changes　**⟳**
- Refine documentation for enhanced clarity.