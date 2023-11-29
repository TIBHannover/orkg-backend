plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation("org.springframework.data:spring-data-commons")

    // Neo4j
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
    }
    // implementation("org.neo4j:neo4j-cypher-dsl:2022.8.5")
    implementation("org.springframework.data:spring-data-neo4j")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.spring.mockk)
            }
        }
    }
}
