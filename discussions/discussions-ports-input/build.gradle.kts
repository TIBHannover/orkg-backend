plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":discussions:discussions-core-model"))

    implementation(project(":common"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.core)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
