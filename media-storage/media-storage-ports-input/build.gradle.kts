plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common"))
    api(project(":media-storage:media-storage-core-model"))
}
