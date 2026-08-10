plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
}

description = "Localize ICU4J integration module."

dependencies {
    api(project(":Localize"))
    implementation(libs.icu4j)
}
