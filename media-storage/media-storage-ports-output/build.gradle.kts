plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":media-storage:media-storage-core-model"))

    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(testFixtures(project(":media-storage:media-storage-core-model")))
    testFixturesImplementation("org.springframework:spring-core")
}
