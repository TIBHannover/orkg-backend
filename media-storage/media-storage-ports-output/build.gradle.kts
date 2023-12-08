plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    api(project(":media-storage:media-storage-core-model"))

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(testFixtures(project(":media-storage:media-storage-core-model")))
    testFixturesImplementation(project(":community:community-ports-input"))
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(libs.javax.activation)
}
