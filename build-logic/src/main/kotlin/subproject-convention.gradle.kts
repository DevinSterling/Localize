plugins {
    alias(libs.plugins.javamodularity)
}

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion = ProjectInfo.JAVA_VERSION
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
    withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).tags(
            "apiNote:a:API Note:",
            "implNote:a:Implementation Note:",
            "implSpec:a:Implementation Requirements:",
        )
        javadocTool = javaToolchains.javadocToolFor {
            languageVersion = ProjectInfo.JAVADOC_VERSION
        }
    }
}
