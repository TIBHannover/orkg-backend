import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("lifecycle-base")
    id("org.orkg.gradle.dependency-analysis-root")
    id("com.diffplug.spotless-changelog")
    id("com.github.ben-manes.versions")
    id("com.osacky.doctor")
}

doctor {
    javaHome {
        failOnError.set(false)
        ensureJavaHomeMatches.set(false)
        ensureJavaHomeIsSet.set(false)
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkConstraints = true
    checkBuildEnvironmentConstraints = true
    checkForGradleUpdate = true

    rejectVersionIf {
        isNonStable(candidate.version) || isSpringManaged(candidate, currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

fun isSpringManaged(
    candidate: ModuleComponentIdentifier,
    currentVersion: String,
): Boolean =
    candidate.version != currentVersion &&
        candidate.group in
        setOf(
            "ch.qos.logback",
            "tools.jackson.core",
            "tools.jackson.datatype",
            "tools.jackson.module",
            "com.github.ben-manes.caffeine",
            "io.micrometer",
            "jakarta.activation",
            "jakarta.persistence",
            "jakarta.validation",
            "jakarta.xml.bind",
            "org.apache.tomcat.embed",
            "org.assertj",
            "org.hamcrest",
            // "org.jetbrains.kotlin", // TODO: Uncomment once Spring Boot provides Kotlin 2.x
            "org.junit.jupiter",
            "org.junit.platform",
            "org.liquibase",
            "org.neo4j", // via spring-boot-data-starter-neo4j
            "org.neo4j.driver",
            "org.slf4j",
            "org.springframework",
            "org.springframework.data",
            "org.springframework.restdocs",
            "org.springframework.security",
            "org.testcontainers",
        )
