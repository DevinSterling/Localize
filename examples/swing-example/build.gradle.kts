plugins {
    java
    application
    id("subproject-convention")
}

application {
    mainModule = "com.devinsterling.localize.example"
    mainClass = "com.devinsterling.localize.example.ClickCount"
}

dependencies {
    implementation(project(":swing"))
}

tasks {
    withType<Javadoc>().configureEach {
        enabled = false
    }
}
