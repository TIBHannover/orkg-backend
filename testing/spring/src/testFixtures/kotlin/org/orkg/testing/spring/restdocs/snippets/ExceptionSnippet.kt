package org.orkg.testing.spring.restdocs.snippets

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.restdocs.RestDocumentationContext
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.snippet.StandardWriterResolver
import org.springframework.restdocs.templates.TemplateFormat
import kotlin.reflect.KClass

class ExceptionSnippet private constructor(
    private val exceptions: List<KClass<out Throwable>>,
) : Snippet {
    override fun document(operation: Operation) {
        val context = operation.attributes[RestDocumentationContext::class.java.name] as RestDocumentationContext
        val placeholderResolverFactory = RestDocumentationContextPlaceholderResolverFactory()
        val writerResolver = StandardWriterResolver(placeholderResolverFactory, Charsets.UTF_8.name(), JsonTemplateFormat)
        val writer = writerResolver.resolve(operation.getName(), "exceptions", context)
        writer.use { writer ->
            writer.append(objectMapper.writeValueAsString(exceptions.map { it.simpleName!! }))
        }
    }

    companion object {
        private val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

        fun exceptions(exceptions: List<KClass<out Throwable>>) = ExceptionSnippet(exceptions)

        private object JsonTemplateFormat : TemplateFormat {
            override fun getId(): String = "json"

            override fun getFileExtension(): String = "json"
        }
    }
}
