plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-context")
    api(project(":media-storage:media-storage-core-model"))
    api(project(":media-storage:media-storage-ports-output"))
    implementation("jakarta.activation:jakarta.activation-api")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(project(":common"))

    containerTestApi("org.junit.jupiter:junit-jupiter-api")
    containerTestApi("org.springframework.boot:spring-boot-test-autoconfigure")
    containerTestApi("org.springframework:spring-test")
    containerTestApi(project(":media-storage:media-storage-ports-output"))
    containerTestApi(testFixtures(project(":media-storage:media-storage-ports-output")))
    containerTestApi(testFixtures(project(":testing:spring")))
    containerTestImplementation("org.springframework:spring-beans")
}

testing {
    suites {
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation(project(":migrations:liquibase"))
                runtimeOnly(libs.kotest.extensions.spring)
                runtimeOnly(libs.liquibase)
                runtimeOnly(libs.postgres.driver)
                runtimeOnly("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7
            }
        }
    }
}
