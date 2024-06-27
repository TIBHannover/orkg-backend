plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons:2.7.16")
    api(project(":graph:graph-core-model"))
}
