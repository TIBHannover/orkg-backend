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
    implementation("org.springframework:spring-core")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(project(":common"))

    containerTestCompileOnly("org.junit.jupiter:junit-jupiter-api")
    containerTestApi("org.springframework.boot:spring-boot-test-autoconfigure")
    containerTestApi("org.springframework:spring-test")
    containerTestApi(project(":media-storage:media-storage-ports-output"))
    containerTestApi(testFixtures(project(":media-storage:media-storage-ports-output")))
    containerTestApi(testFixtures(project(":testing:spring")))
}

testing {
    suites {
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
                runtimeOnly(project(":migrations:liquibase"))
                runtimeOnly("io.kotest.extensions:kotest-extensions-spring")
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                implementation("org.springframework:spring-beans")
                runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
            }
        }
    }
}
