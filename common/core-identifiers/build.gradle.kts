plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi(testFixtures(project(":testing:spring")))
    testFixturesImplementation("jakarta.validation:jakarta.validation-api")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":common:serialization"))
                compileOnly(testFixtures(project(":common:serialization")))
            }
        }
    }
}
