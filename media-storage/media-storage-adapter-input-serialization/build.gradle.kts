plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common")) // for exception
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework:spring-context")
    implementation(libs.jackson.databind)
}
