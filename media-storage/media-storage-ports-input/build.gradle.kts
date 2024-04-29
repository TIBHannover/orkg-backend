plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":media-storage:media-storage-core-model"))

    implementation(project(":common"))

    api(libs.javax.activation) // uses MimeType in public API
}
