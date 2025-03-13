plugins {
    java
    application
    alias(libs.plugins.javafx.plugin)
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
    implementation(project(":Localize"))
    implementation(project(":LocalizeFX"))
}

tasks {
    withType<Javadoc>().configureEach {
        enabled = false
    }
}
