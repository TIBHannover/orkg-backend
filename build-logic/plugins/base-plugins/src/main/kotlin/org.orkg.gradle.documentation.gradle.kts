plugins {
    id("org.antora")
    id("org.openapi.generator")
    id("org.orkg.gradle.base")
    id("com.epages.restdocs-api-spec")
    id("com.github.node-gradle.node") // required to configure antora task in build script
    id("io.spring.antora.generate-antora-yml")
}

val asciidoctor by configurations.creating
