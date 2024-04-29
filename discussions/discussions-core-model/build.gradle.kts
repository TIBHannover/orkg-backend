plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {

    implementation(project(":common"))

    implementation("org.springframework:spring-web")
}
