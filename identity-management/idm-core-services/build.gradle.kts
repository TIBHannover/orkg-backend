// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation(project(":identity-management:idm-ports-input"))
    implementation(project(":identity-management:idm-ports-output"))

    implementation(project(":common")) // for exceptions (UserNotFound)

    implementation("org.springframework.security:spring-security-crypto") // for PasswordEncoder
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.assertj:assertj-core")
            }
        }
    }
}
