rootProject.name = "Localize"

pluginManagement {
    includeBuild("build-logic")
}

include("base", "javafx", "icu4j")
project(":base").name = "Localize"
project(":javafx").name = "LocalizeFX"
project(":icu4j").name = "Localize ICU4J"
