// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))

    implementation(project(":identity-management:idm-core-model")) // for GravatarId, TODO: break dependency
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core) // for JsonProperty

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":identity-management:idm-core-model"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(project(":community:community-adapter-output-spring-data-jpa"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("org.assertj:assertj-core")
            }
        }
    }
}
