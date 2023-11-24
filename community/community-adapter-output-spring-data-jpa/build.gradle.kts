// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":identity-management:idm-ports-output"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":identity-management:idm-core-services"))
    implementation(project(":identity-management:idm-adapter-output-spring-data-jpa")) // TODO: break dependency
    implementation(project(":common"))
    implementation(project(":community:community-core-model"))
    implementation(project(":community:community-ports-output"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.assertj.core)
            }
        }
        val containerTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.FUNCTIONAL_TEST)
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(libs.kotest.extensions.spring)
                implementation(libs.spring.boot.starter.neo4j.migrations)
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":community:community-ports-output")))
                implementation(project(":community:community-ports-output"))
                implementation(project(":community:community-adapter-output-spring-data-jpa"))
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
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}
