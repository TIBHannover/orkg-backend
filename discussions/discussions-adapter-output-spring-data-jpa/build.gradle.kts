plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

val liquibase: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val implementation: Configuration by configurations.getting {
    extendsFrom(liquibase)
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-services"))
    implementation(project(":discussions:discussions-core-model"))
    implementation(project(":discussions:discussions-ports-output"))


    // TODO: needed? -> yes, because not all components moved

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    liquibase(project(mapOf("path" to ":migrations:liquibase", "configuration" to "liquibase")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
        val containerTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.FUNCTIONAL_TEST)
            useJUnitJupiter()
            dependencies {
                implementation(project())
                //implementation(libs.kotest.extensions.spring)
                //implementation(libs.spring.boot.starter.neo4j.migrations)
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":discussions:discussions-ports-output")))
                //implementation(testFixtures(project(":graph:graph-core-services")))
                implementation(project(":discussions:discussions-ports-output"))
                implementation(project(":identity-management:idm-ports-output"))
                implementation(project(":identity-management:idm-adapter-output-spring-data-jpa")) // TODO: break dependency
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7
                implementation("org.postgresql:postgresql")
                implementation(libs.liquibase)
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
