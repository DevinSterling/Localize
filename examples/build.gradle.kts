plugins {
    java
    application
    alias(libs.plugins.javafx.plugin)
}

application {
    mainModule = "com.devinsterling.localize.examples"
    mainClass = "com.devinsterling.localize.examples.ClickCount"
}

javafx {
    version = "17"
    modules = listOf("javafx.controls")
}

dependencies {
    implementation(project(":base"))
    implementation(project(":javafx"))
}

tasks {
    withType<Javadoc>().configureEach {
        enabled = false
    }
}
