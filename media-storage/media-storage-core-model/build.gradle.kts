plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {

    implementation(project(":common"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")

    implementation(libs.jackson.databind)
    implementation(libs.javax.activation)

    testFixturesImplementation(testFixtures(project(":testing:spring"))) // for fixedClock
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-ports-input"))
    testFixturesImplementation(project(":community:community-ports-input"))
    testFixturesImplementation(libs.javax.activation)
    testFixturesImplementation("org.springframework:spring-web")
}
