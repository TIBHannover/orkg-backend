plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    id("org.orkg.jackson-conventions")
    alias(libs.plugins.spotless)
    id("org.orkg.neo4j-conventions") // to obtain version of spring-data commons. TODO: remove after upgrade
}

dependencies {
    api(platform(project(":platform")))

    compileOnly(project(":common"))
    compileOnly(project(":graph:graph-core-model"))
    compileOnly(project(":identity-management:idm-core-model"))

    implementation(project(":content-types:content-types-core-model"))

    // for PageRequests object
    implementation("org.springframework.data:spring-data-commons")

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
    testFixturesImplementation(project(":identity-management:idm-core-model"))

    testFixturesImplementation(project(":content-types:content-types-core-model"))

    // for PageRequests object
    testFixturesImplementation("org.springframework.data:spring-data-commons:2.7.16")
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
}
