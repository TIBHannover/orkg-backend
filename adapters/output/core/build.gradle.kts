plugins  {
    id("org.orkg.kotlin-conventions")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":application:core"))

    implementation(project(":application:shared"))

    val springDataNeo4jVersion = "5.3.4"
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.data:spring-data-neo4j:$springDataNeo4jVersion.RELEASE")

    // FIXME: should go after refactoring
    implementation("org.eclipse.rdf4j:rdf4j-client:3.6.3")
}
