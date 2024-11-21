plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))

    testFixturesApi(project(":common"))
    testFixturesApi(project(":content-types:content-types-core-model"))
    testFixturesApi(project(":content-types:content-types-ports-input"))
    testFixturesImplementation(libs.forkhandles.values4k)
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":content-types:content-types-core-model")))
    testFixturesImplementation("org.eclipse.rdf4j:rdf4j-common-io")
}
