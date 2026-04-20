import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.orkg.gradle.openapi")
}

@CacheableTask
abstract class GenerateOpenApiSpecPythonTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
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

@CacheableTask
abstract class GeneratePythonClientTask : GenerateTask {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val additionalFilesDirectory: DirectoryProperty

    @get:Input
    abstract val additionalDependencies: MapProperty<String, String>

    @Inject
    @Suppress("unused")
    constructor(objectFactory: ObjectFactory) : super(objectFactory) {
        additionalFilesDirectory.convention(project.layout.projectDirectory.dir("src/main/python"))
        // We cannot override the default task action function doWork() because it is final, so we use doLast instead
        doLast { customizePythonClient() }
    }

    private fun customizePythonClient() {
        val outputDir = File(outputDir.get())
        additionalFilesDirectory.get().asFile.copyRecursively(File(outputDir, "orkg_client"))
        addDependenciesToFile(File(outputDir, "pyproject.toml"), "dependencies = [\n") { name, version -> "  \"$name (>= $version)\"," }
        addDependenciesToFile(File(outputDir, "requirements.txt")) { name, version -> "$name >= $version" }
        addDependenciesToFile(File(outputDir, "setup.py"), "REQUIRES = [\n") { name, version -> "    \"$name >= $version\"," }
        removeMetaFiles(outputDir)
    }

    private fun addDependenciesToFile(file: File, location: String? = null, dependencyFormatter: (String, String) -> String) {
        val dependencies = additionalDependencies.get().entries
            .joinToString(separator = "\n", postfix = "\n") { (name, version) -> dependencyFormatter(name, version) }
        val text = StringBuilder(file.readText())
        val index = if (location != null) {
            val index = text.indexOf(location)
            if (index == -1) {
                throw IllegalArgumentException("""Location "$location" does not exist in file "$file".""")
            }
            index + location.length
        } else {
            0
        }
        text.insert(index, dependencies)
        file.writeText(text.toString())
    }

    private fun removeMetaFiles(outputDir: File) {
        File(outputDir, ".openapi-generator").deleteRecursively()
        File(outputDir, ".openapi-generator-ignore").delete()
    }
}

tasks {
    val generateOpenApiSpecPython by registering(GenerateOpenApiSpecPythonTask::class) {
        inputFile.set(project(":documentation").layout.buildDirectory.file("api-spec/openapi3.yaml"))
        dependsOn(":documentation:openapi3")
    }

    register<GeneratePythonClientTask>("generatePythonClient") {
        generatorName.set("python")
        inputSpec.set(layout.buildDirectory.file("api-spec-python/openapi3.yaml").get().asFile.path)
        outputDir.set(layout.buildDirectory.dir("python-client").get().asFile.path)
        httpUserAgent = "ORKG-Python-Client/${project.version}"
        // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/python.md
        configOptions = mapOf(
            "packageName" to "orkg_client",
            "packageVersion" to project.version.toString(),
            "useOneOfDiscriminatorLookup" to "true",
        )
        additionalDependencies = mapOf(
            "python-keycloak" to "7.1.1",
        )
        dependsOn(generateOpenApiSpecPython)
    }
}
