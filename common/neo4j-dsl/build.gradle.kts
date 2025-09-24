@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.neo4j.driver:neo4j-java-driver")
    api("org.neo4j:neo4j-cypher-dsl")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-neo4j")
    api("org.springframework:spring-context")
    api("org.apiguardian:apiguardian-api")
    implementation("org.jetbrains:annotations")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                compileOnly("com.ninja-squad:springmockk")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(testFixtures(project(":common:testing")))
            }
        }
    }
}
