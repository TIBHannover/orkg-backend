plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common"))
    api(project(":graph:graph-core-model"))

    api("org.springframework.data:spring-data-commons")

    testFixturesApi(project(":common"))
    testFixturesApi(project(":graph:graph-core-model"))
}
