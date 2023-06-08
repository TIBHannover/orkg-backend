// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    alias(libs.plugins.spotless)
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))
    api(project(":common:exceptions"))
    implementation(project(":identity-management:idm-application"))
    implementation(project(":identity-management:idm-adapter-output-spring-data-jpa"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-core") // Spring MimeType
    implementation(libs.javax.activation)
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Disable JUnit 4 (aka Vintage)
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
        // exclude(module = "mockito-core")
    }
    // Search string parsing
    implementation("org.apache.lucene:lucene-queryparser:9.5.0")
    // RDF
    implementation("org.eclipse.rdf4j:rdf4j-client:3.7.7") {
        exclude(group = "commons-collections", module = "commons-collections") // Version 3, vulnerable
    }

    testFixturesApi(project(":identity-management:idm-adapter-output-spring-data-jpa"))
    testFixturesApi(libs.bundles.kotest)
    testFixturesApi("org.springframework.data:spring-data-commons")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.forkhandles.values4k)
    testFixturesImplementation(libs.bundles.jaxb)

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: Remove after upgrade
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.contractual)
                implementation(libs.spring.mockk)
            }
        }
    }
}
