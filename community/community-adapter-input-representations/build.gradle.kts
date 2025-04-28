plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:core-identifiers"))
    api(project(":community:community-core-model"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    implementation(project(":common:external-identifiers"))
    implementation(project(":graph:graph-core-constants"))
}
