plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":media-storage:media-storage-core-model"))

    api(libs.javax.activation) // uses MimeType in public API
}
