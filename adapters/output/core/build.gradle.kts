plugins  {
    id("org.orkg.kotlin-conventions")
    `java-test-fixtures`
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

    testFixturesImplementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(module = "neo4j-ogm-http-driver")
    }
    val testContainersVersion = "1.15.3"
    testFixturesImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testFixturesImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testFixturesImplementation("org.testcontainers:neo4j:$testContainersVersion")

    // FIXME: should go after refactoring
    implementation("org.eclipse.rdf4j:rdf4j-client:3.6.3")
}
