plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common")) // for exception
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework:spring-context")
    implementation(libs.jackson.databind)
}
