plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api("org.springframework:spring-context-support")
    api(project(":notifications:notifications-core-model"))
    api(project(":notifications:notifications-ports-output"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5")
                implementation("io.rest-assured:rest-assured")
                implementation("org.hamcrest:hamcrest")
                implementation("org.jetbrains.kotlin:kotlin-stdlib") // "downgrade" from api()
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.boot:spring-boot-autoconfigure")
                implementation("org.testcontainers:testcontainers")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":notifications:notifications-core-model")))
            }
        }
    }
}
