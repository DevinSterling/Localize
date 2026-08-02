plugins {
    alias(libs.plugins.javamodularity)
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = ProjectInfo.JAVA_VERSION
    targetCompatibility = ProjectInfo.JAVA_VERSION
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
    withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }
}
