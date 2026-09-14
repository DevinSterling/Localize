/// # LocalizeICU4J
/// Localize ICU4J integration module.
///
/// - Repository:
///   [https://github.com/DevinSterling/Localize](https://github.com/DevinSterling/Localize)
/// ___
///
/// ## Usage
/// The integration module automatically registers [`IcuFormatter`][com.devinsterling.localize.icu4j.IcuFormatter]
/// as the default [`LocalizationFormatter`][com.devinsterling.localize.LocalizationFormatter] via SPI.
/// Explicitly changing the formatter through
/// [`Localize#setFormatter`][com.devinsterling.localize.Localize#setFormatter] is not required.
///
/// @see com.devinsterling.localize.icu4j.IcuFormatter
/// @since 2.0
module com.devinsterling.localize.icu4j {
    requires com.devinsterling.localize;
    requires com.ibm.icu;

    exports com.devinsterling.localize.icu4j;

    provides com.devinsterling.localize.spi.LocalizationFormatterProvider
        with com.devinsterling.localize.icu4j.spi.IcuFormatterProvider;
}