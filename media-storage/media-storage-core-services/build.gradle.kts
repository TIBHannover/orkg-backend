@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api(project(":common:spring-data"))
    api(project(":media-storage:media-storage-core-model"))
    api(project(":media-storage:media-storage-ports-input"))
    api(project(":media-storage:media-storage-ports-output"))
    implementation("org.springframework:spring-core")
    implementation(project(":common:core-identifiers"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":common:testing")))
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
            }
        }
    }
}
