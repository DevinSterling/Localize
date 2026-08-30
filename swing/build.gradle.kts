plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
}

description = "Localize Swing integration module."

publishConvention {
    displayName = "Localize Swing"
}

dependencies {
    api(project(":base"))
}
