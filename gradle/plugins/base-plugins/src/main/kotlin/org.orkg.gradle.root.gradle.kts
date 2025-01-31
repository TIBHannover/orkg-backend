import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("lifecycle-base")
    id("org.orkg.gradle.dependency-analysis-root")
    id("com.diffplug.spotless-changelog")
    id("com.github.ben-manes.versions")
    id("com.osacky.doctor")
    id("dev.iurysouza.modulegraph")
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
            "com.fasterxml.jackson.core",
            "com.fasterxml.jackson.datatype",
            "com.fasterxml.jackson.module",
            "jakarta.persistence",
            "jakarta.validation",
            "org.apache.tomcat.embed",
            "org.hamcrest",
            "org.liquibase",
            "org.neo4j.driver",
            "org.slf4j",
        )

// Module Graph plugin configuration

val modulesFile = "./modules.mermaid"

moduleGraphConfig {
    readmePath.set(modulesFile)
    heading = ""
}

val convertModuleGraphCodeBlockToStandaloneFile by tasks.registering {
    mustRunAfter(tasks.named("createModuleGraph"))
    doLast {
        val outputFile = File(modulesFile)
        val content = outputFile.readText().lineSequence()
        val asciidocContent =
            content
                // Remove Markdown code blocks
                .filterNot { it.startsWith("```mermaid") }
                .filterNot { it.startsWith("```") }
                // Filter root module, because it connects to everything and gives no information
                .filterNot { it.contains(": --> :") }
                // Work-around for "graph" being a reserved word, which leads to parsing errors
                .map { it.replace(":g", ":G") }
                // Remove emtpy lines at the top of the file
                .filter { it.isNotBlank() }
                .joinToString("\n")
        outputFile.writeText(asciidocContent)
    }
}

tasks.named("createModuleGraph") {
    doFirst {
        File(modulesFile).delete()
    }
    finalizedBy(convertModuleGraphCodeBlockToStandaloneFile)
}
