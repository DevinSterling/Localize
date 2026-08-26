plugins {
    `maven-publish`
}

interface PublishConventionExtension {
    val displayName: Property<String>
}

val extension = extensions.create<PublishConventionExtension>("publishConvention")

afterEvaluate {
    publishing {
        repositories {
            maven {
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }

        publications.create<MavenPublication>("maven") {
            from(components["java"])

            artifactId = "localize-${project.projectDir.name}"

            pom {
                name = extension.displayName
                description = project.description
                url = ProjectInfo.REPOSITORY
                inceptionYear = ProjectInfo.INCEPTION_YEAR

                developers {
                    developer {
                        id = ProjectInfo.GITHUB_ID
                        name = ProjectInfo.AUTHOR
                    }
                }

                licenses {
                    license {
                        name = ProjectInfo.LICENSE
                        url = ProjectInfo.LICENSE_LINK
                    }
                }

                // Reference: https://maven.apache.org/scm/git.html
                scm {
                    url = ProjectInfo.REPOSITORY
                    connection = "scm:git:${ProjectInfo.REPOSITORY}.git"
                    developerConnection = "scm:git:ssh://github.com/${ProjectInfo.GITHUB_ID}/${ProjectInfo.REPO_NAME}.git"
                }
            }
        }
    }
}
