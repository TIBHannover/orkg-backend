plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.springframework:spring-web")
    api("jakarta.validation:jakarta.validation-api")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-ports-input-legacy"))
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation(project(":community:community-core-model"))
}
