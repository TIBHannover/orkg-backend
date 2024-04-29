plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-core:2.15.3")
    api("org.springframework:spring-context")
    api(libs.jackson.databind)
    api(project(":media-storage:media-storage-core-model"))
    implementation(project(":common")) // for exception
}
