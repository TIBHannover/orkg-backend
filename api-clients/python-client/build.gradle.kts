import Org_orkg_gradle_patch_gradle.ApplyPatchesTask
import Org_orkg_gradle_patch_gradle.GeneratePatchesTask
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.spring.gradle.antora.GenerateAntoraYmlTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.orkg.gradle.openapi")
    id("org.orkg.gradle.patch")
    id("io.spring.antora.generate-antora-yml")
}

@CacheableTask
abstract class GenerateOpenApiSpecPythonTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = "documentation"
        outputFile.convention(project.layout.buildDirectory.file("api-spec-python/openapi3.yaml"))
    }

    @TaskAction
    fun action() {
        val objectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .enable(SerializationFeature.INDENT_OUTPUT)
        val openApiSpec = objectMapper.readTree(inputFile.get().asFile)
        val statusCodesWithoutResponseBody = listOf("201", "204")
        val emptySchema = objectMapper.nodeFactory.objectNode().apply {
            val response = objectMapper.nodeFactory.objectNode().apply {
                set<ObjectNode>("schema", objectMapper.nodeFactory.objectNode())
            }
            set<ObjectNode>("application/json", response)
        }
        openApiSpec.path("paths").forEach { path ->
            path.forEach { method ->
                val responses = method.path("responses")
                statusCodesWithoutResponseBody.forEach { statusCodeWithoutResponseBody ->
                    val status = responses.path(statusCodeWithoutResponseBody)
                    if (responses.size() > 1 && !status.isMissingNode) {
                        status as ObjectNode
                        val content = status.path("content")
                        if (content.isMissingNode) {
                            status.set<ObjectNode>("content", emptySchema)
                        }
                    }
                }
            }
        }
        objectMapper.writeValue(outputFile.asFile.get(), openApiSpec)
    }
}

tasks {
    register<GenerateOpenApiSpecPythonTask>("generateOpenApiSpecPython") {
        inputFile.set(project(":documentation").layout.buildDirectory.file("api-spec/openapi3.yaml"))
        description = "Postprocess the contents of an OpenAPI specification to always include a response schema"
        dependsOn(":documentation:openapi3")
    }

    register<GenerateTask>("generateOpenApiClientBase") {
        generatorName.set("python")
        description = "Generates a Python client library based on an OpenAPI specification"
        inputSpec.set(layout.buildDirectory.file("api-spec-python/openapi3.yaml"))
        outputDir.set(layout.buildDirectory.dir("python-client-clean"))
        httpUserAgent = "ORKG-Python-Client/${project.version}"
        // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/python.md
        configOptions = mapOf(
            "packageName" to "orkg_client",
            "packageVersion" to project.version.toString(),
            "useOneOfDiscriminatorLookup" to "true",
        )
        dependsOn("generateOpenApiSpecPython")
    }

    register<ApplyPatchesTask>("generateOpenApiClient") {
        group = "openapi client generation"
        description = "Generates a Python client library based on an OpenAPI specification"
        originalDirectory.set(named<GenerateTask>("generateOpenApiClientBase").get().outputDir)
        patchesDirectory.set(layout.projectDirectory.dir("src/main/patches"))
        outputDirectory.set(layout.buildDirectory.dir("python-client"))
    }

    register<GeneratePatchesTask>("generatePatches") {
        group = "openapi client generation"
        description = "Generates patches for the Python client library"
        originalDirectory.set(named<GenerateTask>("generateOpenApiClientBase").get().outputDir)
        // We are intentionally not linking generatePatches.patchedDirectory with generateOpenApiClient.outputDirectory,
        // so that generateOpenApiClient does not get executed again, potentially overwriting our changes.
        patchedDirectory.set(layout.buildDirectory.dir("python-client"))
        outputDirectory.set(layout.projectDirectory.dir("src/main/patches"))
    }

    named<GenerateAntoraYmlTask>("generateAntoraYml") {
        group = "documentation"
        setProperty("outputFile", layout.buildDirectory.file("aggregated-antora-content/antora.yml"))
        setProperty("baseAntoraYmlFile", File("src/antora/antora.yml"))
    }
}
