plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":media-storage:media-storage-core-model"))

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(testFixtures(project(":media-storage:media-storage-core-model")))
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(libs.javax.activation)
}
