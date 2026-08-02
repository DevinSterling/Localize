plugins {
    java
    application
    alias(libs.plugins.javafx.plugin)
    id("subproject-convention")
}

application {
    mainModule = "com.devinsterling.localize.examples"
    mainClass = "com.devinsterling.localize.examples.Start"
}

javafx {
    version = libs.versions.javafx.dep.get()
    modules("javafx.controls")
}

dependencies {
    implementation(project(":LocalizeFX"))
}

tasks {
    withType<Javadoc>().configureEach {
        enabled = false
    }
}
