import Org_orkg_gradle_patch_gradle.PatchHelper
import io.spring.gradle.antora.GenerateAntoraYmlTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.orkg.gradle.openapi")
    id("io.spring.antora.generate-antora-yml")
}

@CacheableTask
abstract class GenerateTypeScriptClientTask : GenerateTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val patchesDirectory: DirectoryProperty

    init {
        patchesDirectory.convention(project.layout.projectDirectory.dir("src/main/patches"))
        // We cannot override the default task action function doWork() because it is final, so we use doLast instead
        doLast { customizeTypeScriptClient() }
    }

    private fun customizeTypeScriptClient() {
        val outputDir = outputDir.get().asFile
        PatchHelper.applyPatches(patchesDirectory.get().asFile, outputDir)
    }
}

tasks {
    register<GenerateTypeScriptClientTask>("generateOpenApiClient") {
        dependsOn(":documentation:openapi3")
        generatorName.set("typescript-fetch")
        description = "Generates a TypeScript client library based on an OpenAPI specification"
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

    named<GenerateAntoraYmlTask>("generateAntoraYml") {
        group = "documentation"
        setProperty("outputFile", layout.buildDirectory.file("aggregated-antora-content/antora.yml"))
        setProperty("baseAntoraYmlFile", File("src/antora/antora.yml"))
    }
}
