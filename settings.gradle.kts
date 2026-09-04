rootProject.name = "Localize"

pluginManagement {
    includeBuild("build-logic")
}

// Include examples
file("examples").listFiles()?.forEach {
    if (it.isDirectory) {
        include("examples:${it.name}")
    }
}

include("base", "javafx", "swing", "icu4j")
