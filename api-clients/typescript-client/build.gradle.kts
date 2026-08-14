import Org_orkg_gradle_patch_gradle.ApplyPatchesTask
import Org_orkg_gradle_patch_gradle.GeneratePatchesTask
import io.spring.gradle.antora.GenerateAntoraYmlTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.orkg.gradle.openapi")
    id("org.orkg.gradle.patch")
    id("io.spring.antora.generate-antora-yml")
}

tasks {
    register<GenerateTask>("generateOpenApiClientBase") {
        dependsOn(":documentation:openapi3")
        generatorName.set("typescript-fetch")
        description = "Generates a TypeScript client library based on an OpenAPI specification"
        inputSpec.set(project(":documentation").layout.buildDirectory.file("api-spec/openapi3.yaml"))
        outputDir.set(layout.buildDirectory.dir("typescript-client-clean"))
        httpUserAgent = "ORKG-TypeScript-Client/${project.version}"
        // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/typescript-fetch.md
        configOptions = mapOf(
            "npmName" to "@orkg/orkg-client",
            "npmVersion" to project.version.toString(),
            "licenseName" to "MIT",
            "prefixParameterInterfaces" to "true",
        )
    }

    register<ApplyPatchesTask>("generateOpenApiClient") {
        group = "openapi client generation"
        description = "Generates a TypeScript client library based on an OpenAPI specification"
        originalDirectory.set(named<GenerateTask>("generateOpenApiClientBase").get().outputDir)
        patchesDirectory.set(layout.projectDirectory.dir("src/main/patches"))
        outputDirectory.set(layout.buildDirectory.dir("typescript-client"))
    }

    register<GeneratePatchesTask>("generatePatches") {
        group = "openapi client generation"
        description = "Generates patches for the TypeScript client library"
        originalDirectory.set(named<GenerateTask>("generateOpenApiClientBase").get().outputDir)
        // We are intentionally not linking generatePatches.patchedDirectory with generateOpenApiClient.outputDirectory,
        // so that generateOpenApiClient does not get executed again, potentially overwriting our changes.
        patchedDirectory.set(layout.buildDirectory.dir("typescript-client"))
        outputDirectory.set(layout.projectDirectory.dir("src/main/patches"))
    }

    named<GenerateAntoraYmlTask>("generateAntoraYml") {
        group = "documentation"
        setProperty("outputFile", layout.buildDirectory.file("aggregated-antora-content/antora.yml"))
        setProperty("baseAntoraYmlFile", File("src/antora/antora.yml"))
    }
}
