// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":identity-management:idm-ports-input"))
    api(project(":identity-management:idm-ports-output"))
    api(project(":identity-management:idm-core-model"))

    api("org.springframework.security:spring-security-crypto") // for PasswordEncoder
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-context")
}
