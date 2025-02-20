plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api("org.springframework.data:spring-data-neo4j")
    api(project(":profiling:profiling-core-services"))
    api(project(":profiling:profiling-ports-output"))
    implementation("org.neo4j.driver:neo4j-java-driver")
    implementation(project(":content-types:content-types-ports-output"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":statistics:statistics-ports-output"))
    implementation(project(":curation:curation-ports-output"))
}
