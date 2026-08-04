# Changelog

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