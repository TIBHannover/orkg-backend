package org.orkg.graph.testing.asciidoc

import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

val allowedExtractionMethodValues =
    ExtractionMethod.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")

private val allowedValues =
    VisibilityFilter.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")

fun visibilityFilterRequestParameter(): ParameterDescriptor =
    parameterWithName("visibility")
        .description("The visibility modifier. Must be one of $allowedValues. If it is not provided, it will be determined from the `featured` and `unlisted` request parameters (where available).")
        .optional()

fun legacyVisibilityFilterRequestParameters(): Array<ParameterDescriptor> = arrayOf(
    parameterWithName("featured")
        .description("Return only featured results. Defaults to `false`. (*Deprecated*. See <<visibility-filter,Visibility filter>>.)")
        .optional(),
    parameterWithName("unlisted")
        .description("Return only unlisted results. Defaults to `false`. (*Deprecated*. See <<visibility-filter,Visibility filter>>.)")
        .optional(),
)
