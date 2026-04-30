# Changelog

## 1.2 (2026-04-30)
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
- New `defaultValue` method for `LocalizationValueBuilder` to specify a default value
  to return when a given key is not found.
- `LocalizationRequest` now features an inner static `Builder` class to build requests 
  more ergonomically.

### Changes　**⟳**
- Refine documentation for enhanced clarity.