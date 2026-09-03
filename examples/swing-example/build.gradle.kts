plugins {
    java
    application
}

repositories {
    mavenCentral()
}

application {
    mainModule = "com.devinsterling.localize.example"
    mainClass = "com.devinsterling.localize.example.ClickCount"
}

java {
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

dependencies {
    // implementation("com.devinsterling:localize-swing:2.0.0")
    implementation(project(":swing"))
}
