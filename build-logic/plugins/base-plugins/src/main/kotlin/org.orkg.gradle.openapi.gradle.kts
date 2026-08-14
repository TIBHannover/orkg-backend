import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.orkg.gradle.base")
    id("org.openapi.generator")
}

tasks {
    // Disable default open api client generator task
    named("openApiGenerate") {
        enabled = false
    }

    withType<GenerateTask>().configureEach {
        inputSpec.set(layout.buildDirectory.file("api-spec/openapi3.yaml").get().asFile.path)
        cleanupOutput = true
        removeOperationIdPrefix = true
        gitHost = "gitlab.com"
        gitUserId = "TIBHannover/orkg"
        gitRepoId = "orkg-backend"
    }
}
