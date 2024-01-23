plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":discussions:discussions-ports-input"))
    api(project(":discussions:discussions-ports-output"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":community:community-ports-output"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(libs.spring.mockk)
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}
