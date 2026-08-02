plugins {
    // NOTE: This is a temporary solution to access the type-safe
    //       `libs.versions.toml` object in convention scripts.
    //
    // > Gradle GitHub issue: https://github.com/gradle/gradle/issues/15383
    id("dev.panuszewski.typesafe-conventions") version "0.11.1"
}
