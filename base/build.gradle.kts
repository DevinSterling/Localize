plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
}

description = "An easy-to-use internationalization and localization library."

publishConvention {
    displayName = "Localize"
}

testConvention {
    reflectiveTestImplementation(libs.equalsverifier)
}
