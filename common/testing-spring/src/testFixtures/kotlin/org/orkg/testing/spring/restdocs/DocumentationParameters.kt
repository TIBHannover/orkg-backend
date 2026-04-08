package org.orkg.testing.spring.restdocs

import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.ParameterDescriptorWithType
import com.epages.restdocs.apispec.Schema
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.request.RequestPartDescriptor
import kotlin.reflect.KClass

data class DocumentationParameters(
    val summary: String? = null,
    val description: String? = null,
    val privateResource: Boolean = false,
    val deprecated: Boolean = false,
    val requestSchema: Schema? = null,
    val responseSchema: Schema? = null,
    val requestFields: List<FieldDescriptor> = emptyList(),
    val responseFields: List<FieldDescriptor> = emptyList(),
    val links: List<LinkDescriptor> = emptyList(),
    val pathParameters: List<ParameterDescriptorWithType> = emptyList(),
    val queryParameters: List<ParameterDescriptorWithType> = emptyList(),
    val formParameters: List<ParameterDescriptorWithType> = emptyList(),
    val requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
    val responseHeaders: List<HeaderDescriptorWithType> = emptyList(),
    val requestParts: List<RequestPartDescriptor> = emptyList(),
    val requestPartFields: Map<String, List<FieldDescriptor>> = emptyMap(),
    val tags: Set<String> = emptySet(),
    val throws: List<KClass<out Throwable>> = emptyList(),
)
