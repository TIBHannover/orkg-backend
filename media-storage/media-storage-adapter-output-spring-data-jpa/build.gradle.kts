plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.boot:spring-boot-persistence")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-context")
    api(project(":media-storage:media-storage-core-model"))
    api(project(":media-storage:media-storage-ports-output"))
    implementation("org.springframework:spring-core")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(project(":common:core-identifiers"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                compileOnly("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-jdbc-test")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-orm")
                implementation("org.springframework:spring-test")
                implementation(testFixtures(project(":media-storage:media-storage-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("io.kotest:kotest-extensions-spring")
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                runtimeOnly("org.springframework.boot:spring-boot-liquibase")
                runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                runtimeOnly(project(":migrations:liquibase"))
            }
        }
    }
}
