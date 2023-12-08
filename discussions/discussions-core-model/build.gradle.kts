plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.jackson-conventions")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.springframework:spring-web")
}
