import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("org.orkg.gradle.asciidoctor")
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
    restdocs(project(withSnippets(":widget")))
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
        }
    }
    into(aggregatedSnippetsDir)
}

val asciidoctor by tasks.existing(AsciidoctorTask::class) {
    sourceDir("rest-api")

    // Declare all generated Asciidoc snippets as inputs. This connects the tasks, so dependsOn() is not required.
    // Other outputs are filtered, because they do not affect the output of this task.
    val docSources = files(sourceDir).asFileTree.matching { include("**/*.adoc") }
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
        backends("html5")
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
        )
    )

    sources(
        delegateClosureOf<PatternSet> {
            exclude("parts/**")
            include("*.adoc")
            include("api-doc/*.adoc")
            include("architecture/*.adoc")
            include("references/*.adoc")
        }
    )
}

val packageHTML by tasks.registering(Jar::class) {
    from(asciidoctor)
}

val staticFiles by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

tasks.named("build").configure {
    dependsOn(asciidoctor)
}

artifacts {
    add("staticFiles", packageHTML)
}
