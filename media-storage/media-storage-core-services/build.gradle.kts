plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":media-storage:media-storage-ports-input"))
    implementation(project(":media-storage:media-storage-ports-output"))

    implementation(project(":common"))

    implementation("org.springframework:spring-context")
    implementation(libs.javax.activation)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project(":community:community-ports-input"))
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
                implementation(libs.spring.mockk)
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
                implementation("org.assertj:assertj-core")
                implementation(libs.javax.activation)
                implementation(libs.kotest.runner)
            }
        }
    }
}
