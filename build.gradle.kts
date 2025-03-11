plugins {
    `maven-publish`
    alias(libs.plugins.javamodularity)
}

subprojects {
    group = "com.devinsterling.localize"
    version = "1.0"

    val junitVersion by extra("5.10.2")

    repositories {
        mavenCentral()
    }

    apply {
        plugin(rootProject.libs.plugins.javamodularity.get().pluginId)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks {
        withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
        }
        withType<Javadoc>().configureEach {
            options.encoding = "UTF-8"
        }
        named<Test>("test") {
            useJUnitPlatform()
        }
    }

    if (project.name != "examples") afterEvaluate {
        apply {
            plugin("maven-publish")
        }

        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])

                    pom {
                        url.set("https://github.com/devinsterling/localize")
                        name.set(project.ext["name"] as String)
                        description.set(project.description)
                        licenses {
                            license {
                                name.set("Apache-2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                    }
                }
            }
        }
    }
}