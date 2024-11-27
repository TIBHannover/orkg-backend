plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":graph:graph-core-model"))
}
