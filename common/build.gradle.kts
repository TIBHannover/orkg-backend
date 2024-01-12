// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    api("org.springframework:spring-web")

    implementation("org.apache.tomcat.embed:tomcat-embed-core") // for HttpServletRequest
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.data:spring-data-neo4j") // for UncategorizedNeo4jException
    implementation("org.springframework.security:spring-security-core") // for AccessDeniedException, UserDetails
    implementation("org.springframework:spring-webmvc")

    implementation(libs.jackson.databind)
    implementation(libs.jackson.kotlin)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // for timestamp serialization
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":testing:spring")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // exclude(module = "mockito-core") // TODO: uncomment when migrated to MockK
                }
                implementation(libs.spring.restdocs)
                implementation(libs.assertj.core)
            }
        }
    }
}
