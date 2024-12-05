plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.boot:spring-boot")
    api("org.springframework:spring-context")
    api(project(":profiling:profiling-ports-output"))
    implementation("org.slf4j:slf4j-api")
    implementation(kotlin("reflect"))
    implementation(project(":profiling:profiling-core-model"))
}
