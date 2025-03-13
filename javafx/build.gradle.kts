plugins {
    `java-library`
    alias(libs.plugins.javafx.plugin)
}

description = "Localize JavaFX integration module"

dependencies {
    api(project(":Localize"))
    compileOnly(libs.javafx.graphics)

    testImplementation(libs.javafx.graphics)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
