plugins {
    id("org.orkg.gradle.input-adapter-spring-web") // for RestDocs configuration
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-beans")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation("org.springframework:spring-web")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}
