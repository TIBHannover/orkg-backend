plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    implementation(project(":common"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.databind)
}
