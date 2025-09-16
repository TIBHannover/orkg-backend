package org.orkg.testing.spring.restdocs

import com.epages.restdocs.apispec.FieldDescriptors
import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.ParameterDescriptorWithType
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.epages.restdocs.apispec.Schema.Companion.schema
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.request.ParameterDescriptor
import kotlin.reflect.KClass
import org.orkg.testing.spring.restdocs.pagedResponseFields as pagedResponseFieldsWrapper

/**
 * Based on [com.epages.restdocs.apispec.ResourceSnippetParametersBuilder]
 */
class DocumentationBuilder {
    var summary: String? = null
        protected set
    var description: String? = null
        protected set
    var requestSchema: Schema? = null
        protected set
    var responseSchema: Schema? = null
        protected set
    var privateResource: Boolean = false
        protected set
    var deprecated: Boolean = false
        protected set
    var tags: Set<String> = setOf()
        protected set
    var requestFields: List<FieldDescriptor> = emptyList()
        private set
    var responseFields: List<FieldDescriptor> = emptyList()
        private set
    var links: List<LinkDescriptor> = emptyList()
        private set
    var pathParameters: List<ParameterDescriptorWithType> = emptyList()
        private set
    var queryParameters: List<ParameterDescriptorWithType> = emptyList()
        private set
    var formParameters: List<ParameterDescriptorWithType> = emptyList()
        private set
    var requestHeaders: List<HeaderDescriptorWithType> = emptyList()
        private set
    var responseHeaders: List<HeaderDescriptorWithType> = emptyList()
        private set

    fun summary(summary: String?) {
        this.summary = summary
    }

    fun description(description: String?) {
        this.description = description?.trimIndent()
    }

    fun privateResource(privateResource: Boolean) {
        this.privateResource = privateResource
    }

    fun deprecated(deprecated: Boolean) {
        this.deprecated = deprecated
    }

    fun requestFields(schemaClass: KClass<*>, vararg requestFields: FieldDescriptor) =
        requestFields(schemaClass, requestFields.toList())

    fun requestFields(schemaClass: KClass<*>, requestFields: List<FieldDescriptor>) {
        this.requestSchema = schema(schemaClass.simpleName!!)
        this.requestFields = requestFields
    }

    fun requestFields(schemaClass: KClass<*>, fieldDescriptors: FieldDescriptors) =
        requestFields(schemaClass, fieldDescriptors.fieldDescriptors)

    inline fun <reified T : Any> requestFields(vararg requestFields: FieldDescriptor) =
        requestFields(T::class, requestFields.toList())

    inline fun <reified T : Any> requestFields(requestFields: List<FieldDescriptor>) =
        requestFields(T::class, requestFields)

    inline fun <reified T : Any> requestFields(fieldDescriptors: FieldDescriptors) =
        requestFields(T::class, fieldDescriptors.fieldDescriptors)

    fun responseFields(schemaClass: KClass<*>, vararg responseFields: FieldDescriptor) =
        responseFields(schemaClass, responseFields.toList())

    fun responseFields(schemaClass: KClass<*>, responseFields: List<FieldDescriptor>) {
        this.responseSchema = schema(schemaClass.simpleName!!)
        this.responseFields = responseFields
    }

    fun responseFields(schemaClass: KClass<*>, fieldDescriptors: FieldDescriptors) =
        responseFields(schemaClass, fieldDescriptors.fieldDescriptors)

    inline fun <reified T : Any> responseFields(vararg responseFields: FieldDescriptor) =
        responseFields(T::class, responseFields.toList())

    inline fun <reified T : Any> responseFields(responseFields: List<FieldDescriptor>) =
        responseFields(T::class, responseFields)

    inline fun <reified T : Any> responseFields(fieldDescriptors: FieldDescriptors) =
        responseFields(T::class, fieldDescriptors.fieldDescriptors)

    fun pagedResponseFields(schemaClass: KClass<*>, vararg responseFields: FieldDescriptor) =
        pagedResponseFields(schemaClass, responseFields.toList())

    fun pagedResponseFields(schemaClass: KClass<*>, responseFields: List<FieldDescriptor>) {
        this.responseSchema = schema("PageOf${schemaClass.simpleName!!}s")
        this.responseFields = pagedResponseFieldsWrapper(responseFields, false)
    }

    fun pagedResponseFields(schemaClass: KClass<*>, fieldDescriptors: FieldDescriptors) =
        pagedResponseFields(schemaClass, fieldDescriptors.fieldDescriptors)

    inline fun <reified T : Any> pagedResponseFields(vararg responseFields: FieldDescriptor) =
        pagedResponseFields(T::class, responseFields.toList())

    inline fun <reified T : Any> pagedResponseFields(responseFields: List<FieldDescriptor>) =
        pagedResponseFields(T::class, responseFields)

    inline fun <reified T : Any> pagedResponseFields(fieldDescriptors: FieldDescriptors) =
        pagedResponseFields(T::class, fieldDescriptors.fieldDescriptors)

    fun links(vararg links: LinkDescriptor) =
        links(links.toList())

    fun links(links: List<LinkDescriptor>) {
        this.links = links
    }

    fun pathParameters(vararg pathParameters: ParameterDescriptorWithType) =
        pathParameters(pathParameters.toList())

    fun pathParameters(pathParameters: List<ParameterDescriptorWithType>) {
        this.pathParameters = pathParameters
    }

    fun pathParameters(vararg pathParameters: ParameterDescriptor) =
        pathParameters(pathParameters.map { ParameterDescriptorWithType.fromParameterDescriptor(it) })

    fun queryParameters(vararg requestParameters: ParameterDescriptorWithType) =
        queryParameters(requestParameters.toList())

    fun queryParameters(requestParameters: List<ParameterDescriptorWithType>) {
        this.queryParameters = requestParameters
    }

    fun queryParameters(vararg requestParameters: ParameterDescriptor) =
        queryParameters(requestParameters.map { ParameterDescriptorWithType.fromParameterDescriptor(it) })

    fun formParameters(vararg formParameters: ParameterDescriptorWithType) =
        formParameters(formParameters.toList())

    fun formParameters(formParameters: List<ParameterDescriptorWithType>) {
        this.formParameters = formParameters
    }

    fun formParameters(vararg formParameters: ParameterDescriptor) =
        formParameters(formParameters.map { ParameterDescriptorWithType.fromParameterDescriptor(it) })

    fun requestHeaders(requestHeaders: List<HeaderDescriptorWithType>) {
        this.requestHeaders = requestHeaders
    }

    fun requestHeaders(vararg requestHeaders: HeaderDescriptorWithType) =
        requestHeaders(requestHeaders.toList())

    fun requestHeaders(vararg requestHeaders: HeaderDescriptor) =
        requestHeaders(requestHeaders.map { HeaderDescriptorWithType.fromHeaderDescriptor(it) })

    fun responseHeaders(responseHeaders: List<HeaderDescriptorWithType>) {
        this.responseHeaders = responseHeaders
    }

    fun responseHeaders(vararg responseHeaders: HeaderDescriptorWithType) =
        responseHeaders(responseHeaders.toList())

    fun responseHeaders(vararg responseHeaders: HeaderDescriptor) =
        responseHeaders(responseHeaders.map { HeaderDescriptorWithType.fromHeaderDescriptor(it) })

    fun tag(tag: String) =
        tags(tag)

    fun tags(vararg tags: String) {
        this.tags += tags
    }

    internal fun build() = ResourceSnippetParameters(
        summary = summary,
        description = description,
        privateResource = privateResource,
        deprecated = deprecated,
        requestSchema = requestSchema,
        responseSchema = responseSchema,
        requestFields = requestFields,
        responseFields = responseFields,
        links = links,
        pathParameters = pathParameters,
        queryParameters = queryParameters,
        formParameters = formParameters,
        requestHeaders = requestHeaders,
        responseHeaders = responseHeaders,
        tags = tags
    )
}
