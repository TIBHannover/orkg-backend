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
    implementation(project(":common:identifiers"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
                runtimeOnly(project(":migrations:liquibase"))
                runtimeOnly("io.kotest.extensions:kotest-extensions-spring")
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                implementation("org.springframework:spring-beans")
                runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation(testFixtures(project(":media-storage:media-storage-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
