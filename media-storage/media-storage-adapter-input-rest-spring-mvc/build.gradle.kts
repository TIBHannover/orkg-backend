plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.springframework:spring-context")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":media-storage:media-storage-core-model"))
    implementation(project(":common:spring-webmvc"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-core")
                implementation("org.springframework:spring-test")
                implementation("org.springframework:spring-web")
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}
