plugins {
    id("org.orkg.kotlin-conventions")
    `java-test-fixtures`
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")

    implementation(libs.jackson.databind)
    implementation(libs.javax.activation)


    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-ports-input"))
    testFixturesImplementation(project(":identity-management:idm-core-model"))
    testFixturesImplementation(project(":community:community-ports-input"))
    testFixturesImplementation(libs.javax.activation)
    testFixturesImplementation("org.springframework:spring-web")
}
