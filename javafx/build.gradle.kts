plugins {
    `java-library`
    alias(libs.plugins.javafx.plugin)
}

ext["name"] = "LocalizeFX"
description = "Localize JavaFX integration module"

dependencies {
    implementation(project(":base"))
    compileOnly(libs.javafx.base)

    testImplementation(libs.javafx.base)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
