plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))

    implementation("org.springframework:spring-context")
    implementation(libs.jackson.databind)
}
