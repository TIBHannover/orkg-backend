import com.epages.restdocs.apispec.gradle.OpenApi3Task
import com.epages.restdocs.apispec.gradle.PluginOauth2Configuration
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import groovy.lang.Closure
import io.swagger.v3.oas.models.servers.Server
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.orkg.gradle.openapi")
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
            include("**/exceptions.json") // for OpenAPI documentation
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
        val errorFiles = snippetsDir.asFileTree.matching({ include("errors_*/response-fields.adoc") }).files.sorted()
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

abstract class GenerateOpenApiExceptionSnippetsTask : DefaultTask() {
    @get:InputDirectory
    abstract val snippetsDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "documentation"
        snippetsDir.convention(project.layout.buildDirectory.dir("generated-snippets"))
        outputDir.convention(project.layout.buildDirectory.dir("generated-exception-snippets"))
    }

    @TaskAction
    fun action() {
        outputDir.asFile.get().deleteRecursively()
        val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        val errorSnippetFiles = snippetsDir.asFileTree.matching({ include("errors_*/resource.json") }).files
        val exceptionNameToSnippet = errorSnippetFiles.associate {
            val json = objectMapper.readTree(it)
            val exceptionName = json.path("response").path("schema").path("name").textValue()
            exceptionName to json
        }
        val exceptionFiles = snippetsDir.asFileTree.matching({ include("**/resource.json", "**/exceptions.json") }).files
            .groupBy { it.parentFile.name }
            .filter { it.value.size == 2 }
            .mapValues { (_, value) -> value.sortedBy { it.name } }
        val exceptionSnippets = mutableListOf<JsonNode>()
        exceptionFiles.forEach { (operationId, value) ->
            val exceptions = objectMapper.readValue<List<String>>(value[0])
            val resource = objectMapper.readTree(value[1])
            exceptions.mapNotNull { exceptionNameToSnippet[it] }.forEach { exceptionSnippet ->
                exceptionSnippets.add(
                    resource.deepCopy<ObjectNode>().apply {
                        put("operationId", createErrorSnippetOperationId(operationId, exceptionSnippet))
                        replace("response", exceptionSnippet["response"])
                        val request = get("request") as ObjectNode
                        request.replace("requestFields", arrayNode())
                        request.replace("example", null)
                    }
                )
            }
        }
        exceptionSnippets.forEach { snippet ->
            val outputSnippetFolder = File(outputDir.asFile.get(), snippet["operationId"].textValue())
            val outputSnippetFile = File(outputSnippetFolder, "resource.json")
            if (!outputSnippetFolder.exists()) {
                outputSnippetFolder.mkdirs()
            }
            objectMapper.writeValue(outputSnippetFile, snippet)
        }
    }

    private fun createErrorSnippetOperationId(operationId: String, exceptionSnippet: JsonNode): String =
        operationId + "-" + exceptionSnippet["operationId"].textValue().replaceFirst("errors_", "error-").substringBefore('_')
}

abstract class GenerateOpenApiSpecPythonTask : DefaultTask() {
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

val generateErrorListing by tasks.registering(GenerateErrorListingTask::class) {
    inputs.files(aggregateRestDocsSnippets.get().outputs)
}

val generateOpenApiExceptionSnippets by tasks.registering(GenerateOpenApiExceptionSnippetsTask::class) {
    inputs.files(aggregateRestDocsSnippets.get().outputs)
}

val generateOpenApiSpecPython by tasks.registering(GenerateOpenApiSpecPythonTask::class) {
    inputFile.set(File(openapi3.outputDirectory, "openapi3.${openapi3.format}"))
    dependsOn("openapi3")
}

val aggregatedOpenApiSnippetsDir = layout.buildDirectory.dir("generated-openapi-snippets")

val aggregateOpenApiSnippets by tasks.registering(Sync::class) {
    from(generateOpenApiExceptionSnippets.get().outputs)
    from(aggregateRestDocsSnippets.get().outputs) {
        includeEmptyDirs = false
        include("**/resource.json")
        exclude("errors_*/resource.json")
        exclude("classes_find-by-uri")
        exclude("contributor-identifiers_delete")
        exclude("papers_exists-by-doi")
        exclude("papers_exists-by-title")
    }
    into(aggregatedOpenApiSnippetsDir)
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

artifacts {
    add("staticFiles", packageHTML)
}

val openApiServerUrls = mapOf(
    "http://localhost:8080" to "Local instance",
    "https://incubating.orkg.org" to "Testing instance (latest)",
    "https://sandbox.orkg.org" to "Testing instance (release)",
    "https://orkg.org" to "Production instance",
)
val openApiAuthServerUrl = "https://accounts.orkg.org/realms/orkg"

openapi3 {
    snippetsDirectory = aggregatedOpenApiSnippetsDir.get().asFile.path

    title = "Open Research Knowledge Graph (ORKG) REST API"
    description = title
    version = "${project.version}"
    format = "yaml"
    hiddenEndpointPaths = listOf(
        "/open-api-doc-test"
    )
    setServers(
        openApiServerUrls.map { (url, description) ->
            serverClosure {
                this.url = url
                this.description = description
            }
        }
    )
    oauth2SecuritySchemeDefinition = PluginOauth2Configuration().apply {
        flows = arrayOf(
            "authorizationCode",
            "clientCredentials",
            "implicit",
            "password",
        )
        tokenUrl = "$openApiAuthServerUrl/protocol/openid-connect/token"
        authorizationUrl = "$openApiAuthServerUrl/protocol/openid-connect/auth"
    }
}

tasks {
    // For some unknown reason, it is not possible to configure the task directly.
    // TODO: Try again with new version of the restdocs-api-spec Gradle plugin.
    withType(OpenApi3Task::class).configureEach {
        dependsOn(aggregateOpenApiSnippets)
    }

    withType<GenerateTask>().configureEach {
        setGroup("openapi client generation")
        inputSpec.set(layout.buildDirectory.file("api-spec/openapi3.yaml").get().asFile.path)
        cleanupOutput = true
        removeOperationIdPrefix = true
        gitHost = "gitlab.com"
        gitUserId = "TIBHannover/orkg"
        gitRepoId = "orkg-backend" // TODO: configure for each client?
        dependsOn("openapi3")
    }

    register<GenerateTask>("generateTypescriptClient") {
        generatorName.set("typescript-fetch")
        outputDir.set(layout.buildDirectory.dir("generated-clients/typescript-client").get().asFile.path)
        httpUserAgent = "ORKG-TypeScript-Client/${project.version}"
        // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/typescript-fetch.md
        configOptions = mapOf(
            "npmName" to "orkg-typescript-client",
            "npmVersion" to project.version.toString(),
            "licenseName" to "MIT",
            "prefixParameterInterfaces" to "true",
        )
    }

    register<GenerateTask>("generatePythonClient") {
        generatorName.set("python")
        inputSpec.set(layout.buildDirectory.file("api-spec-python/openapi3.yaml").get().asFile.path)
        outputDir.set(layout.buildDirectory.dir("generated-clients/python-client").get().asFile.path)
        httpUserAgent = "ORKG-Python-Client/${project.version}"
        // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/python.md
        configOptions = mapOf(
            "packageName" to "orkg_python_client",
            "packageVersion" to project.version.toString(),
            "useOneOfDiscriminatorLookup" to "true",
        )
        dependsOn(generateOpenApiSpecPython)
    }

    register<GenerateTask>("generateRClient") {
        generatorName.set("r")
        outputDir.set(layout.buildDirectory.dir("generated-clients/r-client").get().asFile.path)
        httpUserAgent = "ORKG-R-Client/${project.version}"
        // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/r.md
        configOptions = mapOf(
            "packageName" to "orkg_r_client",
            "packageVersion" to project.version.toString(),
        )
    }

    register("generateAllClients") {
        setGroup("openapi client generation")
        dependsOn(
            "generateTypescriptClient",
            "generatePythonClient",
            "generateRClient",
        )
    }
}

@Suppress("UNCHECKED_CAST")
fun serverClosure(action: Server.() -> Unit): Closure<Server> =
    closureOf(action) as Closure<Server>
