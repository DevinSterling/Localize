plugins {
    `maven-publish`
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.javamodularity)
}

allprojects {
    group = "com.devinsterling"
    version = "1.0.0"
}

description = "A Java localization library"

val author = "Devin Sterling"
val gitHubId = "DevinSterling"
val repository = "https://github.com/${gitHubId}/${name}"

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = rootProject.libs.plugins.javamodularity.get().pluginId)

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
        named<Test>("test") {
            useJUnitPlatform()
        }
    }

    // Upload artifact to MavenCentral
    if (project.name != "examples") afterEvaluate {
        apply(plugin = "maven-publish")

        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])

                    artifactId = "localize-${project.projectDir.name}"

                    pom {
                        name = project.name
                        description = project.description
                        url = rootProject.jreleaser.project.links.homepage
                        inceptionYear = rootProject.jreleaser.project.inceptionYear

                        developers {
                            developer {
                                id = gitHubId
                                name = author
                            }
                        }

                        licenses {
                            license {
                                name.set(rootProject.jreleaser.project.license)
                                url.set(rootProject.jreleaser.project.links.license)
                            }
                        }

                        scm {
                            url = repository
                            connection = "scm:git:${repository}.git"
                            developerConnection = "scm:git:ssh://github.com/${gitHubId}/${repository}.git"
                        }
                    }
                }
            }

            repositories {
                maven {
                    url = uri(layout.buildDirectory.dir("staging-deploy"))
                }
            }
        }
    }
}

jreleaser {
    project {
        authors = listOf(author)
        name = rootProject.name
        description = rootProject.description
        license = "Apache-2.0"
        inceptionYear = "2025"

        links {
            homepage = repository
            bugTracker = "${repository}/issues"
            license = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }

    signing {
        active = org.jreleaser.model.Active.ALWAYS
        armored = true
        verify = true
    }

    release {
        github {
            repoOwner = gitHubId
            repoUrl = repository
            branch = "master"
        }
    }

    distributions {
        subprojects.filter { it.name != "examples" }.forEach { subproject ->
            create(subproject.name) {
                artifact {
                    path.set(subproject.tasks.named<Jar>("jar").get().archiveFile.get().asFile)
                }
            }
        }
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = org.jreleaser.model.Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    subprojects.filter { it.name != "examples" }.forEach {
                        stagingRepositories.add("${it.layout.buildDirectory.get()}/staging-deploy")
                    }
                    applyMavenCentralRules = true
                }
            }
        }
    }
}