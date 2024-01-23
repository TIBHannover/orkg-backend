plugins {
    id("org.orkg.gradle.kotlin")
}

dependencies {
    api(project(":common"))

    api("org.neo4j.driver:neo4j-java-driver")
    api("org.neo4j:neo4j-cypher-dsl")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-neo4j")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                compileOnly(libs.spring.mockk)
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
            }
        }
    }
}
