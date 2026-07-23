package org.orkg.graph.testing.asciidoc

import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.PathDirection
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.spring.restdocs.enumValues
import org.orkg.testing.toAsciidoc
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

val allowedExtractionMethodValues =
    ExtractionMethod.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedVisibilityValues =
    Visibility.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedVisibilityFilterValues =
    VisibilityFilter.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedPathDirectionValues =
    PathDirection.entries.sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val allowedThingClassValues =
    listOf("class", "resource", "predicate", "literal").sorted().joinToString(separator = "`, `", prefix = "`", postfix = "`")

val publishedArtifactClasses = Classes.publishedArtifactClasses.map(ThingId::value).sorted().toAsciidoc()

fun visibilityFilterQueryParameter(): ParameterDescriptor =
    parameterWithName("visibility")
        .description("Filter for the visibility modifier. Must be one of $allowedVisibilityFilterValues.")
        .enumValues(VisibilityFilter::class)
        .optional()
