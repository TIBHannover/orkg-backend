package org.orkg.testing.spring.restdocs

import com.epages.restdocs.apispec.FieldDescriptors
import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.ParameterDescriptorWithType
import com.epages.restdocs.apispec.ParameterType
import com.epages.restdocs.apispec.References
import com.epages.restdocs.apispec.Schema
import com.epages.restdocs.apispec.Schema.Companion.schema
import com.epages.restdocs.apispec.SimpleType
import org.springframework.restdocs.constraints.Constraint
import org.springframework.restdocs.constraints.ValidatorConstraintResolver
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestPartDescriptor
import kotlin.reflect.KClass
import org.orkg.testing.spring.restdocs.pagedResponseFields as pagedResponseFieldsWrapper

/**
 * Based on [com.epages.restdocs.apispec.ResourceSnippetParametersBuilder]
 */
class DocumentationBuilder(private val documentationContext: DocumentationContext) {
    private var summary: String? = null
    private var description: String? = null
    private var requestSchema: Schema? = null
    private var responseSchema: Schema? = null
    private var privateResource: Boolean = false
    private var deprecated: Boolean = false
    private var tags: Set<String> = setOf()
    private var requestFields: List<FieldDescriptor> = emptyList()
    private var responseFields: List<FieldDescriptor> = emptyList()
    private var links: List<LinkDescriptor> = emptyList()
    private var pathParameters: List<ParameterDescriptorWithType> = emptyList()
    private var queryParameters: List<ParameterDescriptorWithType> = emptyList()
    private var formParameters: List<ParameterDescriptorWithType> = emptyList()
    private var requestHeaders: List<HeaderDescriptorWithType> = emptyList()
    private var responseHeaders: List<HeaderDescriptorWithType> = emptyList()
    private var requestParts: List<RequestPartDescriptor> = emptyList()
    private var requestPartFields: Map<String, List<FieldDescriptor>> = emptyMap()
    private var throws: List<KClass<out Throwable>> = emptyList()

    private val validatorConstraintResolver = ValidatorConstraintResolver()

    fun summary(summary: String?) {
        this.summary = summary
    }

    fun description(description: String?) {
        this.description = description?.trimIndent()
    }

    fun privateResource(privateResource: Boolean = true) {
        this.privateResource = privateResource
    }

    fun deprecated(deprecated: Boolean = true) {
        this.deprecated = deprecated
    }

    fun requestFields(schemaClass: KClass<*>, vararg requestFields: FieldDescriptor) =
        requestFields(schemaClass, requestFields.toList())

    fun requestFields(schemaClass: KClass<*>, requestFields: List<FieldDescriptor>) {
        this.requestSchema = schema(schemaClass.simpleName!!)
        this.requestFields = requestFields.map { it.enhance(schemaClass) }
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
        this.responseFields = responseFields.map { it.enhance(schemaClass) }
    }

    fun responseFields(schemaClass: KClass<*>, fieldDescriptors: FieldDescriptors) =
        responseFields(schemaClass, fieldDescriptors.fieldDescriptors)

    inline fun <reified T : Any> responseFields(vararg responseFields: FieldDescriptor) =
        responseFields(T::class, responseFields.toList())

    inline fun <reified T : Any> responseFields(responseFields: List<FieldDescriptor>) =
        responseFields(T::class, responseFields)

    inline fun <reified T : Any> responseFields(fieldDescriptors: FieldDescriptors) =
        responseFields(T::class, fieldDescriptors.fieldDescriptors)

    fun simpleResponse(simpleType: SimpleType) {
        this.responseSchema = schema(simpleType.name.lowercase())
    }

    fun responseFields(schemaName: String, references: References) {
        this.responseSchema = schema(schemaName, references)
    }

    fun responseFields(schemaClass: KClass<*>, references: References) {
        this.responseSchema = schema(schemaClass.simpleName!!, references)
    }

    inline fun <reified T : Any> responseFields(references: References) =
        responseFields(T::class, references)

    fun pagedResponseFields(schemaClass: KClass<*>, vararg responseFields: FieldDescriptor) =
        pagedResponseFields(schemaClass, responseFields.toList())

    fun pagedResponseFields(schemaClass: KClass<*>, responseFields: List<FieldDescriptor>) {
        this.responseSchema = schema("PageOf${schemaClass.simpleName!!}s")
        this.responseFields = pagedResponseFieldsWrapper(responseFields.map { it.enhance(schemaClass) }, schemaClass, false)
    }

    fun pagedResponseFields(schemaClass: KClass<*>, fieldDescriptors: FieldDescriptors) =
        pagedResponseFields(schemaClass, fieldDescriptors.fieldDescriptors)

    inline fun <reified T : Any> pagedResponseFields(vararg responseFields: FieldDescriptor) =
        pagedResponseFields(T::class, responseFields.toList())

    inline fun <reified T : Any> pagedResponseFields(responseFields: List<FieldDescriptor>) =
        pagedResponseFields(T::class, responseFields)

    inline fun <reified T : Any> pagedResponseFields(fieldDescriptors: FieldDescriptors) =
        pagedResponseFields(T::class, fieldDescriptors.fieldDescriptors)

    fun listResponseFields(schemaClass: KClass<*>, vararg responseFields: FieldDescriptor) =
        listResponseFields(schemaClass, responseFields.toList())

    fun listResponseFields(schemaClass: KClass<*>, responseFields: List<FieldDescriptor>) {
        this.responseSchema = schema("ListOf${schemaClass.simpleName!!}s")
        this.responseFields = applyPathPrefix("[].", responseFields.map { it.enhance(schemaClass) }) +
            fieldWithPath("[]").description("The list of values.").references(schemaClass)
    }

    fun listResponseFields(schemaClass: KClass<*>, fieldDescriptors: FieldDescriptors) =
        listResponseFields(schemaClass, fieldDescriptors.fieldDescriptors)

    inline fun <reified T : Any> listResponseFields(vararg responseFields: FieldDescriptor) =
        listResponseFields(T::class, responseFields.toList())

    inline fun <reified T : Any> listResponseFields(responseFields: List<FieldDescriptor>) =
        listResponseFields(T::class, responseFields)

    inline fun <reified T : Any> listResponseFields(fieldDescriptors: FieldDescriptors) =
        listResponseFields(T::class, fieldDescriptors.fieldDescriptors)

    fun mapResponseFields(schemaClass: KClass<*>, keyDescription: String, responseFields: List<FieldDescriptor>) {
        this.responseSchema = schema("MapOf${schemaClass.simpleName!!}s")
        this.responseFields = applyPathPrefix("*.", responseFields.map { it.enhance(schemaClass) }) +
            fieldWithPath("*").description(keyDescription).references(schemaClass)
    }

    inline fun <reified T : Any> mapResponseFields(keyDescription: String, responseFields: List<FieldDescriptor>) {
        mapResponseFields(T::class, keyDescription, responseFields)
    }

    inline fun <reified T : Any> mapResponseFields(keyDescription: String, vararg responseFields: FieldDescriptor) {
        mapResponseFields(T::class, keyDescription, responseFields.toList())
    }

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
        pathParameters(pathParameters.map { it.toParameterDescriptorWithType() })

    fun queryParameters(vararg requestParameters: ParameterDescriptorWithType) =
        queryParameters(requestParameters.toList())

    fun queryParameters(requestParameters: List<ParameterDescriptorWithType>) {
        this.queryParameters = requestParameters
    }

    fun queryParameters(vararg requestParameters: ParameterDescriptor) =
        queryParameters(requestParameters.map { it.toParameterDescriptorWithType() })

    fun pagedQueryParameters() =
        pagedQueryParameters(emptyList<ParameterDescriptorWithType>())

    fun pagedQueryParameters(vararg requestParameters: ParameterDescriptorWithType) =
        pagedQueryParameters(requestParameters.toList())

    fun pagedQueryParameters(requestParameters: List<ParameterDescriptorWithType>) {
        queryParameters(
            listOf<ParameterDescriptorWithType>(
                *requestParameters.toTypedArray(),
                ParameterDescriptorWithType("page").type(ParameterType.INTEGER).description("The page number requested, 0-indexed.").size().optional(),
                ParameterDescriptorWithType("size").type(ParameterType.INTEGER).description("The number of elements per page. May be lowered if it exceeds the limit.").size(1).optional(),
                ParameterDescriptorWithType("sort").type(ParameterType.ARRAY).description(
                    """
                    A string in the form "\{property},\{direction}".
                    Sortable properties are dependent on the endpoint.
                    Direction can be "asc" or "desc". Parameter can be repeated multiple times.
                    The sorting is order-dependent.
                    """.trimIndent()
                ).optional(),
            )
        )
    }

    fun pagedQueryParameters(vararg requestParameters: ParameterDescriptor) =
        pagedQueryParameters(requestParameters.toList().map { it.toParameterDescriptorWithType() })

    fun formParameters(vararg formParameters: ParameterDescriptorWithType) =
        formParameters(formParameters.toList())

    fun formParameters(formParameters: List<ParameterDescriptorWithType>) {
        this.formParameters = formParameters
    }

    fun formParameters(vararg formParameters: ParameterDescriptor) =
        formParameters(formParameters.map { it.toParameterDescriptorWithType() })

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

    fun requestParts(schemaName: String, vararg requestParts: RequestPartDescriptor) {
        this.requestSchema = Schema(schemaName)
        this.requestParts = requestParts.toList()
    }

    fun requestPartFields(part: String, descriptors: List<FieldDescriptor>) {
        requestPartFields += part to descriptors.toList()
    }

    fun requestPartFields(part: String, vararg descriptors: FieldDescriptor) {
        requestPartFields(part, descriptors.toList())
    }

    fun requestPartFields(schemaClass: KClass<*>, part: String, descriptors: List<FieldDescriptor>) =
        requestPartFields(part, descriptors.map { it.enhance(schemaClass) })

    fun requestPartFields(schemaClass: KClass<*>, part: String, vararg descriptors: FieldDescriptor) =
        requestPartFields(schemaClass, part, descriptors.toList())

    fun tag(tag: String) =
        tags(tag)

    fun tags(vararg tags: String) {
        this.tags += tags
    }

    fun hasTags() = tags.isNotEmpty()

    fun throws(vararg throws: KClass<out Throwable>) {
        this.throws += throws
    }

    private fun FieldDescriptor.enhance(enclosingClass: KClass<*>): FieldDescriptor {
        val propertyPath = PropertyPathResolver.resolve(path, enclosingClass) ?: return this
        val jType = propertyPath.fieldType
        val kType = jType.kotlin
        val constraints = mutableListOf<Constraint>()
        if (propertyPath.fieldName != null) {
            constraints += validatorConstraintResolver.resolveForProperty(propertyPath.fieldName, propertyPath.enclosingClass)
        }
        documentationContext.applyConstraints(constraints, kType)
        documentationContext.resolveFormat(kType)?.let { format -> format(format) }
        if (!attributes.contains("schemaName")) {
            if (isExternalType(jType)) {
                references(kType)
            } else if (path.isWildCard(jType) && propertyPath.typeArgument != null && isExternalType(propertyPath.typeArgument)) {
                references(propertyPath.typeArgument.kotlin)
            }
        }
        if (!attributes.contains("itemsType") && jType.isArrayOrIterable() && propertyPath.typeArgument != null) {
            val jItemsType = propertyPath.typeArgument
            if (jItemsType.isArrayOrIterable()) {
                arrayItemsType("array")
            } else if (jItemsType.isEnum()) {
                arrayItemsType("enum")
                @Suppress("UNCHECKED_CAST")
                enumValues(jItemsType as Class<out Enum<*>>)
            } else {
                val kItemsType = jItemsType.kotlin
                val itemsTypeName = documentationContext.resolveTypeName(kItemsType)
                documentationContext.applyConstraints(constraints, kItemsType)
                if (jItemsType.isJvmType() || isPrimitiveType(itemsTypeName)) {
                    arrayItemsType(itemsTypeName)
                    documentationContext.resolveFormat(kItemsType)?.let { format -> format(format) }
                } else {
                    arrayItemsType("object")
                    references(kItemsType)
                }
            }
        }
        if (!attributes.contains("enumValues") && jType.isEnum) {
            type("enum")
            @Suppress("UNCHECKED_CAST")
            enumValues(jType as Class<out Enum<*>>)
        }
        if (constraints.isNotEmpty()) {
            constraints(constraints)
        }
        if (type == null && (kType == Long::class || kType == Int::class)) {
            type("integer")
        }
        return this
    }

    private fun String.isWildCard(type: Class<*>): Boolean =
        endsWith("*") && Map::class.java.isAssignableFrom(type)

    private fun Class<*>.isArrayOrIterable(): Boolean =
        isArray || Iterable::class.java.isAssignableFrom(this)

    private fun Class<*>.isJvmType(): Boolean =
        isPrimitive || isArray || packageName.startsWith("java.")

    private fun isPrimitiveType(type: String): Boolean =
        when (type.lowercase()) {
            "string", "number", "integer", "boolean", "array" -> true
            else -> false
        }

    private fun isExternalType(type: Class<*>): Boolean =
        !type.isJvmType() && !documentationContext.hasTypeMapping(type.kotlin) && !type.isEnum

    internal fun build() = DocumentationParameters(
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
        requestParts = requestParts,
        requestPartFields = requestPartFields,
        tags = tags,
        throws = throws,
    )

    companion object {
        private fun ParameterDescriptor.toParameterDescriptorWithType(): ParameterDescriptorWithType {
            val type = attributes.remove("type") as? ParameterType
            val parameterWithType = ParameterDescriptorWithType.fromParameterDescriptor(this)
            if (type != null) {
                parameterWithType.type(type)
            }
            return parameterWithType
        }
    }
}
