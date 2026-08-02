plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
    alias(libs.plugins.javafx.plugin)
}

description = "Localize JavaFX integration module"

dependencies {
    api(project(":Localize"))
    compileOnly(libs.javafx.graphics)
    testImplementation(libs.javafx.graphics)
}
