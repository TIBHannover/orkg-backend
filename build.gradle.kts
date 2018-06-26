import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "eu.tib"
version = "0.0.1-SNAPSHOT"

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java)
    .kotlinPluginVersion

buildscript {
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.1.0")
    }
}

plugins {
    kotlin("jvm") version "1.2.50"
    kotlin("plugin.spring") version "1.2.50"
    id("org.springframework.boot") version "2.0.0.RELEASE"
    id("org.asciidoctor.convert") version "1.5.3"
    id("com.palantir.docker") version "0.19.2"
}

apply {
    plugin("io.spring.dependency-management")
    plugin("org.junit.platform.gradle.plugin")
}

dependencies {
    compile(kotlin("stdlib-jdk8", kotlinVersion))
    compile(kotlin("reflect", kotlinVersion))
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.springframework.boot:spring-boot-starter-data-neo4j")
    compile("org.eclipse.rdf4j:rdf4j-repository-sparql:2.2.4")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin")

    testCompile("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit")
    }
    testCompile("org.springframework.restdocs:spring-restdocs-mockmvc:2.0.1.RELEASE")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.0.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.0.0")

    asciidoctor("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.1.RELEASE")
}

val snippetsDir = file("build/generated-snippets")


asciidoctorj {
    version = "1.5.6" // AsciiDoctor (Ruby!) version
}

allprojects {
    repositories {
        jcenter()
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    "test" {
        outputs.dir(snippetsDir)
    }

    "asciidoctor"(AsciidoctorTask::class) {
        inputs.dir(snippetsDir)
        dependsOn("test")

        //outputs.upToDateWhen { false }
        backends("html5")

        options(mapOf("doctype" to "book"))

        attributes(
            mapOf(
                "source-highlighter" to "highlightjs",
                "coderay-linenums-mode" to "table",
                "toc" to "left",
                "icons" to "font",
                "linkattrs" to "true",
                "encoding" to "utf-8"
            )
        )

        sources(delegateClosureOf<PatternSet> {
            exclude("parts/**")
            include("*.adoc")
            include("api-doc/*.adoc")
        })
    }

    docker {
        dependsOn(tasks["build"])
        name = "orkg/prototype"
        buildArgs(
            mapOf(
                "PROJECT_NAME" to project.name,
                "VERSION" to "$version"
            )
        )
        files(tasks["jar"].outputs)
    }
}
