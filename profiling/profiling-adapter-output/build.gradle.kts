plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-context")
    api(project(":common"))
    api(project(":community:community-adapter-output-spring-data-jpa"))
    api(project(":community:community-ports-output"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))
    api(project(":profiling:profiling-core-model"))
    api(project(":profiling:profiling-ports-output"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // TODO: Can be removed after organization refactoring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":community:community-core-model"))
}
