plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
}

description = "A Java localization library"

dependencies {
    implementation(libs.icu4j)
}
