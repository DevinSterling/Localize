plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
    alias(libs.plugins.javafx.plugin)
}

description = "Localize JavaFX integration module"

javafx {
    version = libs.versions.javafx.dep.get()
    modules("javafx.graphics")
}

dependencies {
    api(project(":Localize"))
}
