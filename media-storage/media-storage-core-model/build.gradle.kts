plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common:identifiers"))
    api(project(":common:spring-webmvc"))
    api("org.springframework:spring-core")
    implementation("org.springframework:spring-web")

    testFixturesApi(project(":common:identifiers"))
    testFixturesApi(project(":community:community-ports-input"))
    testFixturesImplementation("org.springframework:spring-core")
    testFixturesImplementation(testFixtures(project(":common:testing")))
}
