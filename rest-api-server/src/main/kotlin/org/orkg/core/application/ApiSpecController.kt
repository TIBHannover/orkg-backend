package org.orkg.core.application

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.resource.NoResourceFoundException
import tools.jackson.core.JacksonException
import tools.jackson.core.util.DefaultIndenter
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.dataformat.yaml.YAMLWriteFeature
import java.io.File

@RestController
@RequestMapping("/api")
class ApiSpecController(
    @param:Value("\${orkg.api-spec.path}")
    private val apiSpecPath: String?,
    private val objectMapper: ObjectMapper,
    private val yamlMapper: YAMLMapper = createYamlMapper(),
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun json(request: HttpServletRequest): String =
        objectMapper.writer().with(prettyPrinter).writeValueAsString(readApiSpec(request))

    @GetMapping(produces = [MediaType.APPLICATION_YAML_VALUE])
    fun yaml(request: HttpServletRequest): String =
        yamlMapper.writer().with(prettyPrinter).writeValueAsString(readApiSpec(request))

    private fun readApiSpec(request: HttpServletRequest): JsonNode? {
        try {
            if (apiSpecPath != null) {
                val file = File(apiSpecPath)
                if (file.exists()) {
                    if (apiSpecPath.endsWith(".json")) {
                        return objectMapper.readTree(file)
                    } else if (apiSpecPath.endsWith(".yaml") || apiSpecPath.endsWith(".yml")) {
                        return yamlMapper.readTree(file)
                    }
                }
            }
        } catch (e: JacksonException) {
            logger.error("Failed to read api spec.", e)
        }
        throw NoResourceFoundException(HttpMethod.GET, request.requestURI, "/api")
    }

    companion object {
        private fun createYamlMapper() = YAMLMapper.builder()
            .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER)
            .enable(YAMLWriteFeature.MINIMIZE_QUOTES)
            .enable(YAMLWriteFeature.SPLIT_LINES)
            .enable(YAMLWriteFeature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
            .build()

        // We need this because on Windows the default line feed is '\r\n', which makes it difficult to test reliably
        private val prettyPrinter = DefaultPrettyPrinter().withObjectIndenter(DefaultIndenter().withLinefeed("\n"))
    }
}
