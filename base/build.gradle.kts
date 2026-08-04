plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
}

description = "A simple-to-use Java localization library."

dependencies {
    implementation(libs.icu4j)
}
