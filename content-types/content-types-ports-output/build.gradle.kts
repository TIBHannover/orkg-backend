plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common:identifiers"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))

    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesApi(project(":graph:graph-ports-output"))
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.eclipse.rdf4j:rdf4j-common-io")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation("dev.forkhandles:fabrikate4k")
    testFixturesImplementation("io.kotest:kotest-assertions-core")
    testFixturesImplementation("io.kotest:kotest-runner-junit5")
    testFixturesImplementation(project(":common:pagination"))
    testFixturesImplementation(project(":common:identifiers"))
    testFixturesImplementation(project(":content-types:content-types-core-model"))
    testFixturesImplementation(project(":graph:graph-core-constants"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":common:testing")))
    testFixturesImplementation(testFixtures(project(":content-types:content-types-core-model")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
}
