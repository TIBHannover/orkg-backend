import groovy.lang.Closure
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import io.swagger.v3.oas.models.servers.Server

plugins {
    id("org.orkg.gradle.asciidoctor")
    alias(libs.plugins.restdocs.openapi)
}

fun withSnippets(path: String): Map<String, String> = mapOf("path" to path, "configuration" to "restdocs")

val restdocs: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    asciidoctor("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.7.RELEASE")
    restdocs(project(withSnippets(":common")))
    restdocs(project(withSnippets(":rest-api-server")))
    restdocs(project(withSnippets(":graph:graph-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":data-export:data-export-adapters")))
    restdocs(project(withSnippets(":licenses:licenses-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":content-types:content-types-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":community:community-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":statistics:statistics-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":curation:curation-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":widget")))
    asciidoctor("io.spring.asciidoctor.backends:spring-asciidoctor-backends:0.0.7")
}

val aggregatedSnippetsDir = layout.buildDirectory.dir("generated-snippets")

val aggregateRestDocsSnippets by tasks.registering(Copy::class) {
    group = "documentation"

    // Explicitly add a dependency on the configuration, because it will not resolve otherwise.
    dependsOn(restdocs)

    // Obtain the list of ZIP files (and extract them). This only works if the configuration was resolved.
    restdocs.files.forEach {
        from(zipTree(it)) {
            include("**/*.adoc")
            include("**/resource.json") // for OpenAPI documentation
        }
    }
    into(aggregatedSnippetsDir)
}

val subfolders = listOf("api-doc", "architecture", "references")

val asciidoctorCopyAssets by tasks.registering(Copy::class) {
    layout.buildDirectory.dir(asciidoctor.get().outputDir.path)
    into(asciidoctor.get().outputDir)
    subfolders.forEach { subfolder ->
        into(subfolder) {
            from("rest-api") {
                include("css/**", "img/**")
            }
        }
    }
}

val asciidoctor by tasks.existing(AsciidoctorTask::class) {
    sourceDir("rest-api")

    // Declare all generated Asciidoc snippets as inputs. This connects the tasks, so dependsOn() is not required.
    // Other outputs are filtered, because they do not affect the output of this task.
    val docSources = files(sourceDir).asFileTree.matching { include("**/*.adoc", "**/*.css", "**/*.svg", "**/*.html") }
    inputs.files(docSources, aggregateRestDocsSnippets)
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .ignoreEmptyDirectories()
        .withPropertyName("asciidocFiles")

    configurations("asciidoctor")
    // TODO: Use {includedir} in documentation, change strategy afterwards
    baseDirFollowsSourceFile()

    asciidoctorj {
        modules {
            diagram.use()
            diagram.version("2.2.10")
        }
        fatalWarnings(missingIncludes())

        // Work-around for JRE 16+, because Java's internal APIs are no longer available due to JPMS.
        // This should be fixed in the Asciidoctor plugin, but never was.
        inProcess = org.asciidoctor.gradle.base.process.ProcessMode.JAVA_EXEC
        forkOptions {
            jvmArgs(
                "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-opens", "java.base/java.io=ALL-UNNAMED",
                "--add-opens", "java.base/java.security=ALL-UNNAMED",
            )
        }
    }

    // outputs.upToDateWhen { false }
    outputOptions {
        backends("spring-html")
    }

    options(mapOf("doctype" to "book"))

    attributes(
        mapOf(
            "source-highlighter" to "rouge",
            "coderay-linenums-mode" to "table",
            "toc" to "left",
            "icons" to "font",
            "linkattrs" to "true",
            "encoding" to "utf-8",
            "snippets" to aggregatedSnippetsDir,
            "docinfo" to "shared,private"
        )
    )

    sources(
        delegateClosureOf<PatternSet> {
            exclude("parts/**")
            include("*.adoc")
            subfolders.forEach { include("$it/*.adoc") }
        }
    )

    resources {
        from(sourceDir) {
            include("css/**", "img/**")
        }
    }

    // FIXME: resolve potential caching and dependency issues
    finalizedBy("asciidoctorCopyAssets")
}

val packageHTML by tasks.registering(Jar::class) {
    from(asciidoctorCopyAssets)
}

val staticFiles by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

tasks.named("build").configure {
    dependsOn(asciidoctorCopyAssets)
}

artifacts {
    add("staticFiles", packageHTML)
}

openapi3 {
    snippetsDirectory = layout.buildDirectory.dir("generated-snippets").get().asFile.path
    // tagDescriptionsPropertiesFile = layout.projectDirectory.file("rest-api/openapi-tags.yaml").asFile.path

    title = "Open Research Knowledge Graph (ORKG) REST API"
    version = "${project.version}"
    setServer(serverClosure { url = "http://localhost:8080" })
}

@Suppress("UNCHECKED_CAST")
fun serverClosure(action: Server.() -> Unit): Closure<Server> = closureOf(action) as Closure<Server>
