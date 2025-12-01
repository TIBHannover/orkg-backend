package org.orkg.testing.spring

import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.ParameterDescriptorWithType
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.common.configuration.SpringJacksonConfiguration
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.orkg.testing.configuration.UnsecureJwtDecoder
import org.orkg.testing.spring.restdocs.DocumentationBuilder
import org.orkg.testing.spring.restdocs.DocumentationContext
import org.orkg.testing.spring.restdocs.DocumentationParameters
import org.orkg.testing.spring.restdocs.snippets.DescriptionSnippet.Companion.description
import org.orkg.testing.spring.restdocs.snippets.ExceptionSnippet.Companion.exceptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.cookies.CookieDescriptor
import org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName
import org.springframework.restdocs.generate.RestDocumentationGenerator
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.payload.SubsectionDescriptor
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.formParameters
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.restdocs.request.RequestPartDescriptor
import org.springframework.restdocs.snippet.AbstractDescriptor
import org.springframework.restdocs.snippet.Attributes
import org.springframework.restdocs.snippet.Snippet
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class, SpringJacksonConfiguration::class, UnsecureJwtDecoder::class, DocumentationContext::class)
@ExtendWith(RestDocumentationExtension::class)
@TestPropertySource(properties = ["spring.jackson.mapper.sort-properties-alphabetically=true"])
abstract class MockMvcBaseTest(val prefix: String) : MockkBaseTest {
    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var documentationContext: DocumentationContext

    @Deprecated(
        message = "This variable will be changed to private in the future, when migration to the documentation dsl is complete."
    )
    protected lateinit var documentationHandler: RestDocumentationResultHandler

    protected lateinit var mockMvc: MockMvc

    val identifier = "${prefix}_{method-name}"

    @BeforeEach
    fun setup(restDocumentation: RestDocumentationContextProvider) {
        documentationHandler = MockMvcRestDocumentation.document(
            identifier,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
        )

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .apply<DefaultMockMvcBuilder>(MockMvcAuthenticationConfigurer())
            .apply<DefaultMockMvcBuilder>(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .and()
                    .uris().withScheme("https").withHost("incubating.orkg.org").withPort(443)
            )
            .build()
    }

    /**
     * Syntactic sugar to generate the default documentation snippets (`curl-request.adoc`, etc.).
     */
    protected fun generateDefaultDocSnippets(): ResultHandler = documentationHandler

    protected fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)

    protected fun MockHttpServletRequestBuilder.content(body: Any): MockHttpServletRequestBuilder =
        content(body.toContent())

    private fun Any.toContent(): String = if (this is String) this else objectMapper.writeValueAsString(this)

    protected fun MockMultipartHttpServletRequestBuilder.json(
        name: String,
        data: Map<String, Any>,
        originalFileName: String = "$name.json",
    ): MockMultipartHttpServletRequestBuilder =
        file(MockMultipartFile(name, originalFileName, MediaType.APPLICATION_JSON_VALUE, data.toContent().toByteArray()))

    protected fun ResultActions.andDocument(builder: DocumentationBuilder.() -> Unit): ResultActions {
        val documentationParameters = DocumentationBuilder(documentationContext)
            .apply(builder)
            .also { if (!it.hasTags()) it.tag(convertPrefixToTag(prefix)) }
            .build()
        val snippets = mutableListOf<Snippet>(resource(documentationParameters.toResourceSnippetParameters()))
        // We need to manually register custom documentation snippets to the documentation handler.
        if (documentationParameters.requestFields.isNotEmpty()) {
            snippets += requestFields(documentationParameters.requestFields)
        }
        if (documentationParameters.responseFields.isNotEmpty()) {
            snippets += responseFields(documentationParameters.responseFields)
        }
        if (documentationParameters.pathParameters.isNotEmpty()) {
            snippets += pathParameters(toParameterDescriptors(documentationParameters.pathParameters))
        }
        if (documentationParameters.queryParameters.isNotEmpty()) {
            snippets += queryParameters(toParameterDescriptors(documentationParameters.queryParameters))
        }
        if (documentationParameters.formParameters.isNotEmpty()) {
            snippets += formParameters(toParameterDescriptors(documentationParameters.formParameters))
        }
        if (documentationParameters.requestHeaders.isNotEmpty()) {
            snippets += requestHeaders(toHeaderDescriptors(documentationParameters.requestHeaders))
        }
        if (documentationParameters.responseHeaders.isNotEmpty()) {
            snippets += responseHeaders(toHeaderDescriptors(documentationParameters.responseHeaders))
        }
        if (documentationParameters.description != null) {
            snippets += description(documentationParameters.description)
        }
        if (documentationParameters.requestParts.isNotEmpty()) {
            snippets += requestParts(documentationParameters.requestParts)
        }
        if (documentationParameters.requestPartFields.isNotEmpty()) {
            snippets += documentationParameters.requestPartFields.map { requestPartFields(it.key, it.value) }
        }
        if (documentationParameters.throws.isNotEmpty()) {
            snippets += exceptions(documentationParameters.throws)
        }
        return andDo(documentationHandler.document(*snippets.toTypedArray())).andDo(documentationHandler)
    }

    companion object {
        fun get(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.get(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedGetRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.get(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun post(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.post(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedPostRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.post(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun postMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            multipart(POST, urlTemplate, *uriVariables)
                .characterEncoding(Charsets.UTF_8.name()) as MockMultipartHttpServletRequestBuilder

        fun documentedPostMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            postMultipart(urlTemplate, *uriVariables)
                .requestAttr(RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE, urlTemplate) as MockMultipartHttpServletRequestBuilder

        fun put(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.put(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedPutRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.put(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun putMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            multipart(PUT, urlTemplate, *uriVariables)
                .characterEncoding(Charsets.UTF_8.name()) as MockMultipartHttpServletRequestBuilder

        fun documentedPutMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            putMultipart(urlTemplate, *uriVariables)
                .requestAttr(RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE, urlTemplate) as MockMultipartHttpServletRequestBuilder

        fun patch(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.patch(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedPatchRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.patch(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun delete(urlTemplate: String, vararg uriVariables: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.delete(urlTemplate, *uriVariables)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedDeleteRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.delete(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun head(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            MockMvcRequestBuilders.head(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun documentedHeadRequestTo(urlTemplate: String, vararg uriValues: Any): MockHttpServletRequestBuilder =
            RestDocumentationRequestBuilders.head(urlTemplate, *uriValues)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding(Charsets.UTF_8.name())

        fun patchMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            multipart(PATCH, urlTemplate, *uriVariables)
                .characterEncoding(Charsets.UTF_8.name()) as MockMultipartHttpServletRequestBuilder

        fun documentedPatchMultipart(urlTemplate: String, vararg uriVariables: Any): MockMultipartHttpServletRequestBuilder =
            patchMultipart(urlTemplate, *uriVariables)
                .requestAttr(RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE, urlTemplate) as MockMultipartHttpServletRequestBuilder

        fun ResultActions.andPrint(): ResultActions = andDo(MockMvcResultHandlers.print())

        private fun toParameterDescriptors(parameters: List<ParameterDescriptorWithType>) =
            parameters.map { p ->
                parameterWithName(p.name).description(p.description)
                    .apply { if (p.optional) optional() }
                    .apply { if (p.isIgnored) ignored() }
            }

        private fun toHeaderDescriptors(requestHeaders: List<HeaderDescriptorWithType>) =
            requestHeaders.map { h ->
                headerWithName(h.name).description(h.description)
                    .apply { if (h.optional) optional() }
            }

        private val ASCIIDOC_URL_MACRO_REGEX = Regex("""(https?://[^\[\s]+)\[(.*)\]""")
        private val ASCIIDOC_INTERNAL_CROSS_REFERENCE_REGEX = Regex("""<<(?:[A-Za-z0-9-]+,)?([^>]+)>>""")
        private val ASCIIDOC_ADMONITION_BLOCK_REGEX = Regex("""\[(NOTE|TIP|IMPORTANT|CAUTION|WARNING)\]\s*\n(?:\..*?\n)?(={4,})\s*\n([\s\S]*?)\n\2""")

        private fun DocumentationParameters.toResourceSnippetParameters(): ResourceSnippetParameters =
            ResourceSnippetParameters(
                summary = summary,
                description = description?.stripAsciidocFormatting(),
                privateResource = privateResource,
                deprecated = deprecated,
                requestSchema = requestSchema,
                responseSchema = responseSchema,
                requestFields = requestFields.stripAsciidocFormatting(),
                responseFields = responseFields.stripAsciidocFormatting(),
                links = links.stripAsciidocFormatting(),
                pathParameters = pathParameters.stripAsciidocFormatting(),
                queryParameters = queryParameters.stripAsciidocFormatting(),
                formParameters = formParameters.stripAsciidocFormatting(),
                requestHeaders = requestHeaders.stripAsciidocFormatting(),
                responseHeaders = responseHeaders.stripAsciidocFormatting(),
                tags = tags,
                requestParts = requestParts.stripAsciidocFormatting(),
                requestPartFields = requestPartFields.mapValues { it.value.stripAsciidocFormatting() },
            )

        private fun String.stripAsciidocFormatting(): String =
            replace(ASCIIDOC_INTERNAL_CROSS_REFERENCE_REGEX, "$1")
                .replace(ASCIIDOC_URL_MACRO_REGEX, "[$2]($1)") // convert to markdown
                .replace(ASCIIDOC_ADMONITION_BLOCK_REGEX, "$1:\n$3")

        private fun Any?.stripAsciidocFormatting(): Any? =
            if (this is String) stripAsciidocFormatting() else this

        private fun <T : AbstractDescriptor<T>> List<T>.stripAsciidocFormatting(): List<T> =
            map { it.copy().description(it.description.stripAsciidocFormatting()) }

        private fun convertPrefixToTag(prefix: String): String =
            prefix.foldIndexed(StringBuilder()) { index, acc, c ->
                when {
                    index == 0 -> acc.append(c.titlecase())
                    c == '-' -> acc.append(" ")
                    acc.last() == ' ' -> acc.append(c.titlecase())
                    else -> acc.append(c)
                }
            }.toString()

        @Suppress("UNCHECKED_CAST")
        private fun <T : AbstractDescriptor<T>> T.copy(): T {
            val descriptor = when (this) {
                is HeaderDescriptor -> headerWithName(name)
                    .also { if (isOptional) it.optional() }
                is ParameterDescriptor -> parameterWithName(name)
                    .also { if (isOptional) it.optional() }
                    .also { if (isIgnored) it.ignored() }
                is LinkDescriptor -> linkWithRel(rel)
                    .also { if (isOptional) it.optional() }
                    .also { if (isIgnored) it.ignored() }
                is CookieDescriptor -> cookieWithName(name)
                    .also { if (isOptional) it.optional() }
                    .also { if (isIgnored) it.ignored() }
                is SubsectionDescriptor -> subsectionWithPath(path)
                    .type(type)
                    .also { if (isOptional) it.optional() }
                    .also { if (isIgnored) it.ignored() }
                is FieldDescriptor -> fieldWithPath(path)
                    .type(type)
                    .also { if (isOptional) it.optional() }
                    .also { if (isIgnored) it.ignored() }
                is RequestPartDescriptor -> partWithName(name)
                    .also { if (isOptional) it.optional() }
                    .also { if (isIgnored) it.ignored() }
                is ParameterDescriptorWithType -> ParameterDescriptorWithType(name)
                    .type(type)
                    .also {
                        it.defaultValue = defaultValue
                        if (optional) it.optional()
                        if (isIgnored) it.ignored()
                    }
                is HeaderDescriptorWithType -> HeaderDescriptorWithType(name)
                    .type(type)
                    .also {
                        it.defaultValue = defaultValue
                        it.example = example
                        if (optional) it.optional()
                    }
                else -> throw IllegalArgumentException("Unknown descriptor class ${this::class}")
            }

            descriptor.description(description)
            descriptor.attributes(*attributes.map { Attributes.Attribute(it.key, it.value) }.toTypedArray())
            return descriptor as T
        }
    }
}
