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
    api(project(":eventbus"))
    api("org.springframework:spring-beans")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(project(":graph:graph-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))

    containerTestApi("org.junit.jupiter:junit-jupiter-api")
    containerTestApi("org.springframework.boot:spring-boot-test-autoconfigure")
    containerTestApi("org.springframework:spring-test")
    containerTestApi(project(":community:community-ports-output"))
    containerTestApi(project(":eventbus"))
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
                implementation(libs.assertj.core)
                runtimeOnly(project(":migrations:liquibase"))
                runtimeOnly(libs.liquibase)
                runtimeOnly(libs.postgres.driver)
            }
        }
    }
}
