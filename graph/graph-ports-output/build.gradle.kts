plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":graph:graph-core-model"))

    implementation(project(":common"))
    implementation(project(":content-types:content-types-core-model"))

    // for PageRequests object
    implementation("org.springframework.data:spring-data-commons")

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))

    testFixturesImplementation(project(":content-types:content-types-core-model"))

    // for PageRequests object
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
}
