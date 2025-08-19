plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":common:core-identifiers"))
    implementation(project(":common:spring-webmvc"))
    api("com.fasterxml.jackson.core:jackson-core")
    api("jakarta.validation:jakarta.validation-api")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework:spring-context")
    api("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework:spring-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("io.kotest:kotest-common")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-runner-junit5")
            }
        }
    }
}
