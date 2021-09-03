plugins  {
    id("org.orkg.kotlin-conventions")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

dependencies {
    api(project(":application:core"))

    implementation(project(":application:shared"))

    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(module = "neo4j-ogm-http-driver")
    }

    testImplementation(testFixtures(project(":application:core")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    val testContainersVersion = "1.15.3"
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:neo4j:$testContainersVersion")

    // FIXME: should go after refactoring
    implementation("org.eclipse.rdf4j:rdf4j-client:3.6.3")
}
