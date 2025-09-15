plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common:core-identifiers"))
    api(project(":data-import:data-import-core-model"))

    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesImplementation("io.kotest:kotest-assertions-core")
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("io.kotest:kotest-runner-junit5")
    testFixturesImplementation("org.eclipse.rdf4j:rdf4j-common-io")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation("dev.forkhandles:fabrikate4k")
    testFixturesImplementation(project(":common:core-identifiers"))
    testFixturesImplementation(project(":common:string-utils"))
    testFixturesImplementation(project(":data-import:data-import-core-model"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":data-import:data-import-core-model")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}
