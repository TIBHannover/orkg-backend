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
    api(project(":common"))
    api(project(":discussions:discussions-core-model"))
    api(project(":discussions:discussions-ports-output"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("javax.validation:validation-api")
    runtimeOnly(project(":migrations:liquibase"))

    containerTestApi("org.springframework.boot:spring-boot-test-autoconfigure")
    containerTestApi("org.springframework:spring-test")
    containerTestApi(project(":discussions:discussions-ports-output"))
    containerTestApi(testFixtures(project(":discussions:discussions-ports-output")))
    containerTestApi(testFixtures(project(":testing:spring")))
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
                implementation("org.springframework:spring-beans")
                runtimeOnly(project(":migrations:liquibase"))
                runtimeOnly("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7
                runtimeOnly(libs.liquibase)
                runtimeOnly(libs.postgres.driver)
            }
        }
    }
}
