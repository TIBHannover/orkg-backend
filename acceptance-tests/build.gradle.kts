@file:Suppress("UnstableApiUsage")

import java.time.Instant

plugins {
    kotlin("jvm") version "2.2.10"
    id("com.avast.gradle.docker-compose") version "0.17.12"
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
            dependencies {
                implementation(project(":testing-dsl"))
            }
        }
        val test by getting(JvmTestSuite::class)
        val testSetup by registering(JvmTestSuite::class)
    }
}

val isLocalRun = project.findProperty("local")?.toString().toBoolean()
val seedValue = project.findProperty("seed")

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeTags("acceptance-test") // ensures that tests inherit from DslTestCase
    }
    if (isLocalRun) {
        logger.lifecycle("Configuring acceptance tests to be run against local development setup")
        environment(
            mapOf<String, String>(
                // These need to match the values set in compose.acceptance-tests.yaml.
                "API_HOST" to "localhost",
                "API_TCP_8080" to "8080",
                "KEYCLOAK_HOST" to "localhost",
                "KEYCLOAK_TCP_8080" to "8888",
                "MAILSERVER_HOST" to "localhost",
                "MAILSERVER_TCP_8025" to "8025",
            )
        )
    }
    // If we run locally, we either need the seed or have to use a time-based on. If not, we only take the value if passed to us.
    // This is mostly for making sure that "--rerun" does not cause issues due to the fixed seed in the random number generator.
    val seed = if (isLocalRun && seedValue == null) Instant.now().toEpochMilli().toString() else seedValue?.toString()
    if (seed != null) {
        environment("ORKG_ACCEPTANCE_TESTS_RANDOM_SEED" to seed)
        logger.lifecycle("Set random seed to $seed")
    }

    val pairs = layout.settingsDirectory.file("../.env").asFile.readLines()
        .filter { it.isNotBlank() }
        .filterNot { it.startsWith("#") }
        .associate { line ->
            if (!line.contains("=")) error("Error in .env file: No equal sign found in line \"$line\"")
            val (key, value) = line.split("=")
            // We do not remove quotes from the value, which could be an issue. But we do not use that syntax.
            key.trim() to value.trim()
        }
    environment(pairs)
}

tasks.named("test") {
    description = "Runs the acceptance tests."
}

tasks.named("testSetup") {
    description = "Runs basic tests to check if the acceptance tests were setup properly, using Compose."
}

dockerCompose {
    useComposeFiles.set(listOf("../compose.yaml", "../compose.acceptance-tests.yaml"))
    captureContainersOutput.set(true)
    if (!isLocalRun) {
        isRequiredBy(tasks.named("test"))
        isRequiredBy(tasks.named("testSetup"))
    }
}

java {
    // Configure runtime environment explicitly, otherwise the Compose Gradle plugin fails.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
