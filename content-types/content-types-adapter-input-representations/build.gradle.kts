plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:datatypes"))
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":graph:graph-adapter-input-representations"))
    api(project(":graph:graph-core-model"))
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(project(":common:serialization"))
    implementation(project(":common:external-identifiers"))
}
