plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common:identifiers"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))

    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-tx")

    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.eclipse.rdf4j:rdf4j-common-io")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation("dev.forkhandles:fabrikate4k")
    testFixturesImplementation("io.kotest:kotest-assertions-core")
    testFixturesImplementation(project(":common:identifiers"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":common:testing")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}
