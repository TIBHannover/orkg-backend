plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))

    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesApi(project(":graph:graph-ports-output"))
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.kotest.assertions.core)
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":content-types:content-types-core-model"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":content-types:content-types-core-model")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
}
