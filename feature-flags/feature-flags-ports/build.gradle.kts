plugins {
    id("org.orkg.kotlin-conventions")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))
}