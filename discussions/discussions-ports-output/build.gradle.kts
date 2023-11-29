plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":discussions:discussions-core-model"))

    implementation("org.springframework.data:spring-data-commons:2.7.16") // TODO: drop version after upgrade

    testFixturesApi(platform(project(":platform")))
    testFixturesApi(libs.kotest.runner) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(testFixtures(project(":identity-management:idm-core-model")))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":identity-management:idm-ports-output"))
    testFixturesImplementation(project(":identity-management:idm-core-model"))
    testFixturesImplementation(project(":discussions:discussions-core-model"))
    testFixturesImplementation("org.springframework.data:spring-data-commons:2.7.16")  // TODO: drop version after upgrade
}
