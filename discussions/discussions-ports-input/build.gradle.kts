plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common"))
    api(project(":discussions:discussions-core-model"))
}
