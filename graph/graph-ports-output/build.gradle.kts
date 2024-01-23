plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))

    api("org.springframework.data:spring-data-commons")

    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.kotest.assertions.core)
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
}
