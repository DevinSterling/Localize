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
    version = "17"
    modules = listOf("javafx.controls")
}

dependencies {
    implementation(project(":LocalizeFX"))
}

tasks {
    withType<Javadoc>().configureEach {
        enabled = false
    }
}
