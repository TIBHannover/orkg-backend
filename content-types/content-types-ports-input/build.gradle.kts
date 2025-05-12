plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:functional"))
    api(project(":common:core-identifiers"))
    api(project(":common:datatypes"))
    api(project(":community:community-core-model"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))
    implementation(project(":common:external-identifiers"))

    testFixturesApi(project(":common:core-identifiers"))
    testFixturesApi(project(":content-types:content-types-core-model"))
    testFixturesApi(project(":content-types:content-types-ports-input"))
    testFixturesApi(project(":graph:graph-core-model"))
    testFixturesImplementation("dev.forkhandles:values4k")
    testFixturesImplementation(project(":graph:graph-core-constants"))
    testFixturesImplementation(testFixtures(project(":content-types:content-types-core-model")))
    testFixturesImplementation("org.eclipse.rdf4j:rdf4j-common-io")
}
