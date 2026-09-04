plugins {
    java
    application
}

repositories {
    mavenCentral()
}

application {
    mainModule = "com.devinsterling.localize.example"
    mainClass = "com.devinsterling.localize.example.Example"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    // implementation("com.devinsterling:localize-swing:2.0.0")
    implementation(project(":swing"))
}
