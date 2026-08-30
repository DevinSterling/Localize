module com.devinsterling.localize.icu4j {
    requires com.devinsterling.localize;
    requires com.ibm.icu;

    exports com.devinsterling.localize.icu4j;

    provides com.devinsterling.localize.spi.LocalizationFormatterProvider
        with com.devinsterling.localize.icu4j.spi.IcuFormatterProvider;
}