plugins {
    java
    application
    alias(libs.plugins.javafx.plugin)
    id("subproject-convention")
}

application {
    mainModule = "com.devinsterling.localize.example"
    mainClass = "com.devinsterling.localize.example.Start"
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

tasks {
    withType<Javadoc>().configureEach {
        enabled = false
    }
}
