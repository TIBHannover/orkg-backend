import com.epages.restdocs.apispec.gradle.PluginOauth2Configuration
import groovy.lang.Closure
import io.swagger.v3.oas.models.servers.Server
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

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
        if (name != "openApiGenerate") {
            setGroup("openapi client generation")
        }
        inputSpec.set(layout.buildDirectory.file("api-spec/openapi3.yaml").get().asFile.path)
        cleanupOutput = true
        removeOperationIdPrefix = true
        gitHost = "gitlab.com"
        gitUserId = "TIBHannover/orkg"
        gitRepoId = "orkg-backend"
    }
}
