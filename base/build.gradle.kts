plugins {
    `java-library`
}

description = "A Java localization library"

dependencies {
    implementation(libs.icu4j)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}
