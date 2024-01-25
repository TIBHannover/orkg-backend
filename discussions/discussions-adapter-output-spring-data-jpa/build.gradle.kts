plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

val liquibase: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val implementation: Configuration by configurations.getting {
    extendsFrom(liquibase)
}

dependencies {
    implementation(project(":discussions:discussions-ports-output"))

    implementation(project(":common"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    liquibase(project(mapOf("path" to ":migrations:liquibase", "configuration" to "liquibase")))
}

testing {
    suites {
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":discussions:discussions-ports-output")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                runtimeOnly(libs.postgres.driver)
                runtimeOnly(libs.liquibase)
                implementation(project(":migrations:liquibase"))
                implementation("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7
            }
        }
    }
}
