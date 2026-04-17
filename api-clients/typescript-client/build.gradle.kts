import com.epages.restdocs.apispec.gradle.OpenApi3Task
import com.epages.restdocs.apispec.gradle.PluginOauth2Configuration
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import groovy.lang.Closure
import io.swagger.v3.oas.models.servers.Server
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

plugins {
    id("org.orkg.gradle.openapi")
}

tasks {
    register<GenerateTask>("generateTypescriptClient") {
        dependsOn(":documentation:openapi3")
        generatorName.set("typescript-fetch")
        inputSpec.set(project(":documentation").layout.buildDirectory.file("api-spec/openapi3.yaml").get().asFile.path)
        outputDir.set(layout.buildDirectory.dir("typescript-client").get().asFile.path)
        httpUserAgent = "ORKG-TypeScript-Client/${project.version}"
        // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/typescript-fetch.md
        configOptions = mapOf(
            "npmName" to "@orkg/orkg-client",
            "npmVersion" to project.version.toString(),
            "licenseName" to "MIT",
            "prefixParameterInterfaces" to "true",
        )
    }
}
