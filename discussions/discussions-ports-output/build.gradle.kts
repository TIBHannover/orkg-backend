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

    api(project(":discussions:discussions-core-model"))

    implementation(project(":common"))

    implementation("org.springframework.data:spring-data-commons:2.7.16") // TODO: drop version after upgrade

    testFixturesApi(platform(project(":platform")))
    testFixturesApi(libs.kotest.runner) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":discussions:discussions-core-model"))
    testFixturesImplementation("org.springframework.data:spring-data-commons:2.7.16")  // TODO: drop version after upgrade
}
