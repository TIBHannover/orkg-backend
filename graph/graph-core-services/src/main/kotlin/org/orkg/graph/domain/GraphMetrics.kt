package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.statistics.domain.CachedMetric
import org.orkg.statistics.domain.Metric
import org.orkg.statistics.domain.MultiValueParameterSpec
import org.orkg.statistics.domain.SingleValueParameterSpec
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val subjectClassesParameter = MultiValueParameterSpec(
    name = "Subject classes filter",
    description = "Filter for subject classes.",
    type = ThingId::class,
    parser = ::ThingId
)

private val subjectIdParameter = SingleValueParameterSpec(
    name = "Subject id filter",
    description = "Filter for subject id.",
    type = ThingId::class,
    parser = ::ThingId
)

private val subjectLabelParameter = SingleValueParameterSpec(
    name = "Subject label filter",
    description = "Filter for subject label.",
    type = String::class,
    parser = { it }
)

private val predicateIdParameter = SingleValueParameterSpec(
    name = "Predicate id filter",
    description = "Filter for predicate id.",
    type = ThingId::class,
    parser = ::ThingId
)

private val objectClassesParameter = MultiValueParameterSpec(
    name = "Object classes filter",
    description = "Filter for object classes.",
    type = ThingId::class,
    parser = ::ThingId
)

private val objectIdParameter = SingleValueParameterSpec(
    name = "Object id filter",
    description = "Filter for object id.",
    type = ThingId::class,
    parser = ::ThingId
)

private val objectLabelParameter = SingleValueParameterSpec(
    name = "Object label filter",
    description = "Filter for object label.",
    type = String::class,
    parser = { it }
)

private val labelParameter = SingleValueParameterSpec(
    name = "Label filter",
    description = "Filter for label.",
    type = String::class,
    parser = { it }
)

private val exactParameter = SingleValueParameterSpec(
    name = "Exact label filter",
    description = "Whether label filtering should be exact.",
    type = Boolean::class,
    parser = { it.toBoolean() }
)

private val observatoryIdParameter = SingleValueParameterSpec(
    name = "Observatory id filter",
    description = "Filter for observatory id.",
    type = ObservatoryId::class,
    parser = ::ObservatoryId
)

private val organizationIdParameter = SingleValueParameterSpec(
    name = "Organization id filter",
    description = "Filter for organization id.",
    type = OrganizationId::class,
    parser = ::OrganizationId
)

private val includeParameter = MultiValueParameterSpec(
    name = "Include class filter",
    description = "A set of class ids that the thing must have.",
    type = ThingId::class,
    parser = ::ThingId
)

private val excludeParameter = MultiValueParameterSpec(
    name = "Exclude class filter",
    description = "A set of class ids that the thing must not have.",
    type = ThingId::class,
    parser = ::ThingId
)

private val createdByParameter = SingleValueParameterSpec(
    name = "Created by filter",
    description = "Filter for the original creator.",
    type = ContributorId::class,
    parser = ::ContributorId
)

private val createdAtStartParameter = SingleValueParameterSpec(
    name = "Creation time start filter",
    description = "Filter for the created at timestamp, marking the oldest timestamp.",
    type = OffsetDateTime::class,
    parser = { OffsetDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
)

private val createdAtEndParameter = SingleValueParameterSpec(
    name = "Creation time end filter",
    description = "Filter for the created at timestamp, marking the most recent timestamp.",
    type = OffsetDateTime::class,
    parser = { OffsetDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
)

private val visibilityParameter = SingleValueParameterSpec(
    name = "Visibility filter",
    description = "Filter for visibility.",
    type = VisibilityFilter::class,
    values = VisibilityFilter.entries,
    parser = VisibilityFilter::valueOf
)

@Configuration
class GraphMetrics {
    @Bean
    fun thingCountMetric(
        thingRepository: ThingRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "thing-count",
        description = "Number of things in the graph.",
        group = "things",
        parameterSpecs = mapOf(
            "q" to labelParameter,
            "exact" to exactParameter,
            "visibility" to visibilityParameter,
            "created_by" to createdByParameter,
            "created_at_start" to createdAtStartParameter,
            "created_at_end" to createdAtEndParameter,
            "include" to includeParameter,
            "exclude" to excludeParameter,
            "observatory_id" to observatoryIdParameter,
            "organization_id" to organizationIdParameter,
        ),
        supplier = { parameters ->
            thingRepository.count(
                label = parameters[labelParameter]?.let { SearchString.of(it, parameters[exactParameter] == true) },
                visibility = parameters[visibilityParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                includeClasses = parameters[includeParameter]?.toSet().orEmpty(),
                excludeClasses = parameters[excludeParameter]?.toSet().orEmpty(),
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
            )
        }
    )

    @Bean
    fun statementCountMetric(
        statementRepository: StatementRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "statement-count",
        description = "Number of statements in the graph.",
        group = "things",
        parameterSpecs = mapOf(
            "subject_classes" to subjectClassesParameter,
            "subject_id" to subjectIdParameter,
            "subject_label" to subjectLabelParameter,
            "predicate_id" to predicateIdParameter,
            "object_classes" to objectClassesParameter,
            "object_id" to objectIdParameter,
            "object_label" to objectLabelParameter,
            "created_by" to createdByParameter,
            "created_at_start" to createdAtStartParameter,
            "created_at_end" to createdAtEndParameter,
        ),
        supplier = { parameters ->
            statementRepository.count(
                subjectClasses = parameters[subjectClassesParameter]?.toSet().orEmpty(),
                subjectId = parameters[subjectIdParameter],
                subjectLabel = parameters[subjectLabelParameter],
                predicateId = parameters[predicateIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                objectClasses = parameters[objectClassesParameter]?.toSet().orEmpty(),
                objectId = parameters[objectIdParameter],
                objectLabel = parameters[objectLabelParameter],
            )
        }
    )
}
