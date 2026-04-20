import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

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
