plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common"))
    implementation("org.springframework:spring-web")

    testFixturesApi(project(":common"))
    testFixturesApi(project(":community:community-ports-input"))
    testFixturesImplementation("jakarta.activation:jakarta.activation-api")
    testFixturesImplementation("org.springframework:spring-core")
    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
}
