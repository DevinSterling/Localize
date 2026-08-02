rootProject.name = "Localize"

pluginManagement {
    includeBuild("build-logic")
}

include("base", "javafx", "examples")
project(":base").name = "Localize"
project(":javafx").name = "LocalizeFX"
