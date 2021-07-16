plugins  {
    id("org.orkg.kotlin-conventions")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":application:core"))

    val springDataNeo4jVersion = "5.3.4"
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.data:spring-data-neo4j:$springDataNeo4jVersion.RELEASE")
}
