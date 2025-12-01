package org.orkg.graph.testing.asciidoc

import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.spring.restdocs.enumValues
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

val allowedExtractionMethodValues =
    ExtractionMethod.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedVisibilityValues =
    Visibility.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedVisibilityFilterValues =
    VisibilityFilter.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedThingClassValues =
    listOf("class", "resource", "predicate", "literal").sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

fun visibilityFilterQueryParameter(): ParameterDescriptor =
    parameterWithName("visibility")
        .description("Filter for the visibility modifier. Must be one of $allowedVisibilityFilterValues.")
        .enumValues(VisibilityFilter::class)
        .optional()

fun legacyVisibilityFilterRequestParameters(): Array<ParameterDescriptor> = arrayOf(
    parameterWithName("featured")
        .description("Return only featured results. Defaults to `false`. (*Deprecated*. See <<visibility-filter,Visibility filter>>.)")
        .optional(),
    parameterWithName("unlisted")
        .description("Return only unlisted results. Defaults to `false`. (*Deprecated*. See <<visibility-filter,Visibility filter>>.)")
        .optional(),
)
