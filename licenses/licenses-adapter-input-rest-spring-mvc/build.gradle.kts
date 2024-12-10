plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation("com.ninja-squad:springmockk")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation(project(":common:spring-webmvc"))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}

dependencies {
    api("org.springframework:spring-web")
    api(project(":licenses:licenses-ports-input"))
    implementation(project(":licenses:licenses-core-model"))
}
