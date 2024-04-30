plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common"))
    implementation("org.springframework:spring-web")
}
