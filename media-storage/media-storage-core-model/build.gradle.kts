plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(libs.javax.activation)
    api(project(":common"))
    implementation("org.springframework:spring-web")

    testFixturesApi(project(":common"))
    testFixturesApi(project(":community:community-ports-input"))
    testFixturesImplementation("org.springframework:spring-core")
    testFixturesImplementation(libs.javax.activation)
    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
}
