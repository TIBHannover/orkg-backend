plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api(project(":media-storage:media-storage-core-model"))
    api(project(":media-storage:media-storage-ports-input"))
    api(project(":media-storage:media-storage-ports-output"))
    implementation(libs.javax.activation)
    implementation(project(":common"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":community:community-ports-input"))
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
            }
        }
    }
}
