// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
                implementation(project(":common:serialization"))
                implementation(project(":content-types:content-types-core-model"))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // exclude(module = "mockito-core") // TODO: uncomment when migrated to MockK
                }
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("jakarta.activation:jakarta.activation-api")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
            }
        }
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.apache.tomcat.embed:tomcat-embed-core")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(libs.jackson.databind)
    api("jakarta.validation:jakarta.validation-api")
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-output")) // uses repository directly
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":media-storage:media-storage-ports-input"))
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(project(":media-storage:media-storage-core-model"))
    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
}
