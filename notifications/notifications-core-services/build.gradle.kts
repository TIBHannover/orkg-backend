plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api("org.freemarker:freemarker")
    api(project(":notifications:notifications-core-model"))
    api(project(":notifications:notifications-ports-input"))
    api(project(":notifications:notifications-ports-output"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.commons:commons-text")
    implementation("org.slf4j:slf4j-api")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":notifications:notifications-core-model")))
            }
        }
    }
}
