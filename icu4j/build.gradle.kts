plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
}

description = "Localize ICU4J integration module."

publishConvention {
    displayName = "Localize ICU4J"
}

dependencies {
    api(project(":base"))
    implementation(libs.icu4j)
}
