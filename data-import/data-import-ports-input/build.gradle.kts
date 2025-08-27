plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.springframework.batch:spring-batch-core")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:core-identifiers"))
    api(project(":data-import:data-import-core-model"))

    testFixturesApi(project(":data-import:data-import-ports-input"))
    testFixturesImplementation(project(":data-import:data-import-core-model"))
    testFixturesImplementation(testFixtures(project(":common:testing")))
}
