plugins {
    alias(libs.plugins.jreleaser)
}

jreleaser {
    project {
        authors = listOf(ProjectInfo.AUTHOR)
        name = rootProject.name
        description = rootProject.description
        license = ProjectInfo.LICENSE
        inceptionYear = ProjectInfo.INCEPTION_YEAR
        tags = listOf("Localization", "i18n", "JavaFX")

        links {
            homepage = ProjectInfo.REPOSITORY
            bugTracker = "${ProjectInfo.REPOSITORY}/issues"
            license = ProjectInfo.LICENSE_LINK
        }
    }

    signing {
        active = org.jreleaser.model.Active.ALWAYS

        pgp {
            armored = true
            verify = true
        }
    }

    release.github {
        repoOwner = ProjectInfo.GITHUB_ID
        repoUrl = ProjectInfo.REPOSITORY
        branch = "main"
    }
}

gradle.projectsEvaluated {
    // Retrieve all publishable projects
    val publishedProjects = rootProject.subprojects.filter {
        it.pluginManager.hasPlugin("publishing-conventions")
    }

    jreleaser {
        distributions {
            publishedProjects.forEach {
                create(it.name).artifact {
                    path = it.tasks.named<Jar>("jar").get().archiveFile.get().asFile
                }
            }
        }

        deploy.maven.mavenCentral.create("sonatype") {
            active = org.jreleaser.model.Active.ALWAYS
            url = "https://central.sonatype.com/api/v1/publisher"
            publishedProjects.forEach {
                stagingRepositories.add("${it.layout.buildDirectory.get()}/staging-deploy")
            }
            applyMavenCentralRules = true
        }
    }
}
