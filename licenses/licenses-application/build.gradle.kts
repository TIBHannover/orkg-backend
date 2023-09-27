plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("jacoco")
    id("org.orkg.neo4j-conventions")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common:exceptions"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.spring.mockk)
                implementation("org.assertj:assertj-core")
            }
        }
    }
}

// TODO: The following section was copied from the convention plugin and can be deleted when using Gradle 8+

tasks {
    withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
        }
    }

    // Create reproducible archives
    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
