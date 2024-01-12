plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation("org.apache.commons:commons-lang3")
}
