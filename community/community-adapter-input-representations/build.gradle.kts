plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common:core-identifiers"))
    api(project(":community:community-core-model"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation(project(":common:external-identifiers"))
    implementation(project(":graph:graph-core-constants"))
}
