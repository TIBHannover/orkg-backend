plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.springframework:spring-web")
}
