plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    implementation(project(":common")) // for exception
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework:spring-context")
    implementation(libs.jackson.databind)
}
