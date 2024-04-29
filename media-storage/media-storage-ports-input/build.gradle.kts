plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(libs.javax.activation) // uses MimeType in public API
    api(project(":common"))
    api(project(":media-storage:media-storage-core-model"))
}
