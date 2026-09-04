plugins {
    `java-library`
    jacoco
    id("test-convention")
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}
