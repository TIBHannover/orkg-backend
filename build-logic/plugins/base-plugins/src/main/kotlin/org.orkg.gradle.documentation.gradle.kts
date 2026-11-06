plugins {
    id("org.antora")
    id("org.orkg.gradle.base")
    id("org.orkg.gradle.openapi")
    id("com.epages.restdocs-api-spec")
    id("com.github.node-gradle.node") // required to configure antora task in build script
    id("io.spring.antora.generate-antora-yml")
}

val asciidoctor by configurations.creating
