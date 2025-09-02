import com.epages.restdocs.apispec.gradle.OpenApi3Task
import groovy.lang.Closure
import io.swagger.v3.oas.models.servers.Server
import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("org.orkg.gradle.asciidoctor")
    id("java-library")
}

fun withSnippets(path: String): Map<String, String> = mapOf("path" to path, "configuration" to "restdocs")

val restdocs: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    asciidoctor("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.7.RELEASE")
    restdocs(project(withSnippets(":common:pagination")))
    restdocs(project(withSnippets(":common:spring-webmvc")))
    restdocs(project(withSnippets(":rest-api-server")))
    restdocs(project(withSnippets(":graph:graph-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":data-export:data-export-adapters")))
    restdocs(project(withSnippets(":data-import:data-import-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":content-types:content-types-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":community:community-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":statistics:statistics-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":curation:curation-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":notifications:notifications-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":media-storage:media-storage-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":identity-management:idm-adapter-input-rest-spring-security-legacy")))
    restdocs(project(withSnippets(":widget")))
    asciidoctor("io.spring.asciidoctor.backends:spring-asciidoctor-backends:0.0.7")
}

val aggregatedSnippetsDir = layout.buildDirectory.dir("generated-snippets")

val aggregateRestDocsSnippets by tasks.registering(Sync::class) {
    group = "documentation"

    // Explicitly add a dependency on the configuration, because it will not resolve otherwise.
    dependsOn(restdocs)

    // Obtain the list of ZIP files (and extract them). This only works if the configuration was resolved.
    restdocs.files.forEach {
        from(zipTree(it)) {
            // TODO: extract to copyspec, use "with" from gradle
            include("**/*.adoc")
            include("**/resource.json") // for OpenAPI documentation
        }
    }
    into(aggregatedSnippetsDir)
}

abstract class GenerateErrorListingTask : DefaultTask() {
    @get:InputDirectory
    abstract val snippetsDir: DirectoryProperty

    @get:OutputFile
    abstract val errorListing: RegularFileProperty

    init {
        group = "documentation"
        snippetsDir.convention(project.layout.buildDirectory.dir("generated-snippets"))
        errorListing.set(project.layout.buildDirectory.file("error-snippets/all-errors.adoc"))
    }

    companion object {
        private val typeRegex = Regex("Always `([a-z:_]+)` for this error.")
    }

    @TaskAction
    fun action() {
        val errorFiles = snippetsDir.asFileTree.matching({ include("errors-*/response-fields.adoc") }).files.sorted()
        val listingFile = errorListing.asFile.get()
        val snippetsDir = snippetsDir.asFile.get()
        listingFile.writer(Charsets.UTF_8).use { writer ->
            // Customization of text is done in the documentation, this only produces a simple list of snippets.
            // Headings are used to separate entries, and may need adjustments using "leveloffset" on include.
            errorFiles.forEach { file ->
                val type = findTypeFromText(file)
                val anchorType = type.replace(":", "-").replace("_", "-")
                writer.write(
                    """
                    |[discrete,#error-$anchorType]
                    |== $type
                    |[cols="1,1,3"]
                    |include::{snippets}/${file.toRelativeString(snippetsDir)}[]
                    |
                    |
                    """.trimMargin()
                )
            }
        }
    }

    private fun findTypeFromText(file: File): String = file.useLines { lines ->
        val line = lines.firstOrNull { typeRegex.containsMatchIn(it) } ?: throw RuntimeException("Cannot find a line containing the type!")
        // We already know that we can match, so ignoring null handling
        typeRegex.find(line)!!.groups[1]!!.value
    }
}

val generateErrorListing by tasks.registering(GenerateErrorListingTask::class) {
    inputs.files(aggregateRestDocsSnippets.get().outputs)
}

val subfolders = listOf("api-doc", "architecture", "references")

val asciidoctor by tasks.existing(AsciidoctorTask::class) {
    setSourceDir(file("rest-api"))

    // Declare all generated Asciidoc snippets as inputs. This connects the tasks, so dependsOn() is not required.
    // Other outputs are filtered, because they do not affect the output of this task.
    val docSources = files(sourceDir).asFileTree.matching { include("**/*.adoc", "**/*.css", "**/*.svg", "**/*.html") }
    inputs.files(docSources, aggregateRestDocsSnippets, generateErrorListing)
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .ignoreEmptyDirectories()
        .withPropertyName("asciidocFiles")

    configurations("asciidoctor")
    // TODO: Use {includedir} in documentation, change strategy afterwards
    baseDirFollowsSourceFile()

    asciidoctorj {
        modules {
            diagram.use()
        }
        fatalWarnings(missingIncludes())

        // Work-around for JRE 16+, because Java's internal APIs are no longer available due to JPMS.
        // This should be fixed in the Asciidoctor plugin, but never was.
        jvm {
            jvmArgs(
                "--add-opens",
                "java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-opens",
                "java.base/java.io=ALL-UNNAMED",
                "--add-opens",
                "java.base/java.security=ALL-UNNAMED",
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
            "author" to "The Open Research Knowledge Graph (ORKG) project",
            "revnumber" to project(":rest-api-server").version,
            "source-highlighter" to "rouge",
            "coderay-linenums-mode" to "table",
            "toc" to "left",
            "icons" to "font",
            "linkattrs" to "true",
            "encoding" to "utf-8",
            "snippets" to aggregatedSnippetsDir.get(),
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
            subfolders.forEach { subfolder ->
                include("$subfolder/css/**", "$subfolder/img/**")
            }
        }
        subfolders.forEach { subfolder ->
            into(subfolder) {
                from(sourceDir) {
                    include("css/**", "img/**")
                }
            }
        }
    }
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

openapi3 {
    snippetsDirectory = layout.buildDirectory.dir("generated-snippets").get().asFile.path
    // tagDescriptionsPropertiesFile = layout.projectDirectory.file("rest-api/openapi-tags.yaml").asFile.path

    title = "Open Research Knowledge Graph (ORKG) REST API"
    version = "${project.version}"
    setServer(serverClosure { url = "http://localhost:8080" })
}

tasks {
    // For some unknown reason, it is not possible to configure the task directly.
    // TODO: Try again with new version of the restdocs-api-spec Gradle plugin.
    withType(OpenApi3Task::class).configureEach {
        dependsOn(asciidoctor)
    }
}

@Suppress("UNCHECKED_CAST")
fun serverClosure(action: Server.() -> Unit): Closure<Server> = closureOf(action) as Closure<Server>
