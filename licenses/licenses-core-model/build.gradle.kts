plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common:spring-webmvc"))
    implementation("org.springframework:spring-web")
}
