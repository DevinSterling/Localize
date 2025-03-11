plugins {
    `java-library`
}

ext["name"] = "Localize"
description = "A Java localization library"

dependencies {
    api(libs.slf4j.api)
    implementation(libs.icu4j)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
