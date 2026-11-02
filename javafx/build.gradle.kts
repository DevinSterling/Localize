plugins {
    id("java-library-convention")
    id("subproject-convention")
    id("publish-convention")
    alias(libs.plugins.javafx.plugin)
}

description = "Localize JavaFX integration module."

publishConvention {
    displayName = "LocalizeFX"
}

javafx {
    version = libs.versions.javafx.dep.get()
    modules("javafx.graphics")
}

dependencies {
    api(project(":base"))
}

val headlessTestPattern = "**/HeadlessTest.*"
val headlessTest = tasks.register<Test>("headlessTest") {
    description = "Runs headless tests with uninitialized JavaFX toolkit"
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform()
    include(headlessTestPattern)
}

tasks.test {
    exclude(headlessTestPattern)
}

tasks.check {
    dependsOn(headlessTest)
}

tasks.jacocoTestReport {
    dependsOn(headlessTest)
    executionData(headlessTest.get())
}
