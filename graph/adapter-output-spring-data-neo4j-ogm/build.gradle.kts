// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")


plugins {
    id("org.orkg.kotlin-conventions")
    alias(libs.plugins.spring.boot) apply false
    kotlin("plugin.spring")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:application")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation("com.ninja-squad:springmockk:2.0.1")
            }
        }
    }
}

dependencies {
    api(project(":library")) // TODO: remove when domain was moved
    api(platform(project(":platform")))

    api(project(":graph:application"))

    // Pagination (e.g. Page, Pageable, etc.)
    api("org.springframework.data:spring-data-commons")

    // Forkhandles
    implementation(libs.forkhandles.values4k)

    // Neo4j
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(module = "neo4j-ogm-http-driver")
    }
    implementation("org.neo4j:neo4j-ogm-bolt-native-types")

    // Caching
    api("org.springframework.boot:spring-boot-starter-cache")
}
