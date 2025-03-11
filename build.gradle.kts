plugins {
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.javamodularity)
}

allprojects {
    group = "com.devinsterling.localize"
    version = "1.0.0"
}

subprojects {
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
            plugin(rootProject.libs.plugins.jreleaser.get().pluginId)
        }

        val gradleProject = project

        jreleaser {
            project {
                authors = listOf("Devin Sterling")
                name = gradleProject.ext["name"] as String
                description = gradleProject.description
                license = "Apache-2.0"
                inceptionYear = "2025"

                links {
                    homepage = "https://github.com/DevinsterLing/Localize"
                    license = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    bugTracker = "https://github.com/DevinsterLing/Localize/issues"
                }
            }

            signing {
                active = org.jreleaser.model.Active.ALWAYS
                armored = true
            }

            release {
                github {
                    commitAuthor {
                        name = "Devin Sterling"
                    }
                }
            }

            deploy {
                maven {
                    mavenCentral {
                        create("sonatype") {
                            stagingRepository("target/staging-deploy")
                            active = org.jreleaser.model.Active.ALWAYS
                            url = "https://central.sonatype.com/api/v1/publisher"
                        }
                    }
                }
            }
        }
    }
}