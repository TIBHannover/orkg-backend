plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common:core-identifiers"))
    api(project(":media-storage:media-storage-core-model"))
    api("org.springframework:spring-core")
}
