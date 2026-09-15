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
        javadocTool = javaToolchains.javadocToolFor {
            languageVersion = ProjectInfo.JAVADOC_VERSION
        }
        options {
            this as StandardJavadocDocletOptions
            encoding = "UTF-8"
            tags(
                "apiNote:a:API Note:",
                "implNote:a:Implementation Note:",
                "implSpec:a:Implementation Requirements:",
            )

            // By default, Gradle shows non-exported modules in the generated Javadoc.
            // The following is a fix that only shows exported modules:
            // https://github.com/gradle/gradle/issues/19726
            val sourceSetDirectories = sourceSets.main.get().java.sourceDirectories.joinToString(":")
            addStringOption("-source-path", sourceSetDirectories)
            // Internal modules to not show
            exclude("**/impl/**")
        }
    }
}
