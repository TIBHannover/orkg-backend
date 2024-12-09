plugins {
    id("org.orkg.gradle.kotlin-library")
}

// We use the "unit test" configuration for adapters as well, because Gradle is currently not able to aggregate several
// test suites into one report. (See https://github.com/gradle/gradle/issues/23223.) For all intents and purposes, the
// adapters should run against the infrastructures they provide, so this is their "unit". Single-class unit tests can
// still be added. They can be separated by JUnit tags to run them independently.
//
// This plugin is kept to add some semantics to the build file, and in case we need to customize test configurations of
// adapter test separately.
