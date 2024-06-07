// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-context")
    api("org.springframework:spring-tx")
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-output"))
    api(project(":identity-management:idm-adapter-output-spring-data-jpa")) // TODO: break dependency
    api(project(":identity-management:idm-core-model"))
    api(project(":identity-management:idm-ports-output")) // transitive
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(libs.jakarta.validation)
    implementation(project(":graph:graph-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))

    containerTestApi("org.junit.jupiter:junit-jupiter-api")
    containerTestApi("org.springframework.boot:spring-boot-test-autoconfigure")
    containerTestApi("org.springframework:spring-test")
    containerTestApi(project(":community:community-ports-output"))
    containerTestApi(project(":identity-management:idm-adapter-output-spring-data-jpa"))
    containerTestApi(project(":identity-management:idm-ports-output"))
    containerTestApi(testFixtures(project(":community:community-ports-output")))
    containerTestApi(testFixtures(project(":testing:spring")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(libs.assertj.core)
                implementation("org.junit.jupiter:junit-jupiter-api")
            }
        }
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation("org.springframework:spring-beans")
                implementation(project(":migrations:liquibase"))
                runtimeOnly("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove or make version-less after upgrade to 2.7
                runtimeOnly(libs.liquibase)
                runtimeOnly(libs.postgres.driver)
            }
        }
    }
}
