// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.container-testing-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":community:community-ports-output"))

    implementation(project(":identity-management:idm-ports-output")) // for UserRepository, TODO: break dependency
    implementation(project(":identity-management:idm-adapter-output-spring-data-jpa")) // TODO: break dependency

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(libs.jakarta.validation)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(libs.assertj.core)
            }
        }
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(libs.kotest.extensions.spring)
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":community:community-ports-output")))
                implementation(project(":community:community-ports-output"))
                implementation(project(":identity-management:idm-adapter-output-spring-data-jpa"))
                implementation(project(":identity-management:idm-ports-output"))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("org.springframework.data:spring-data-jpa")
                runtimeOnly(libs.postgres.driver)
                runtimeOnly(libs.liquibase)
                implementation(project(":migrations:liquibase"))
                implementation("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7
            }
        }
    }
}
