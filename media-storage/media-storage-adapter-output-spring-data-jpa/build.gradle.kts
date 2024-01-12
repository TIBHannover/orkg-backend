plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    implementation(project(":media-storage:media-storage-core-model"))
    implementation(project(":media-storage:media-storage-ports-output"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation(libs.bundles.jaxb)
}

testing {
    suites {
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(libs.kotest.extensions.spring)
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":media-storage:media-storage-ports-output")))
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
