plugins {
    java
    application
    // id("org.openjfx.javafxplugin") version "0.1.0"
    alias(libs.plugins.javafx.plugin)
}

repositories {
    mavenCentral()
}

application {
    mainModule = "com.devinsterling.localize.example"
    mainClass = "com.devinsterling.localize.example.ClickCount"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

javafx {
    version = libs.versions.javafx.dep.get()
    modules("javafx.controls")
}

dependencies {
    // implementation("com.devinsterling:localize-javafx:2.0.0")
    implementation(project(":javafx"))
    // implementation("com.devinsterling:localize-icu4j:2.0.0")
    implementation(project(":icu4j"))
}
