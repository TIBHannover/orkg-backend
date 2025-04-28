package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.output.StatementRepository
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

@Configuration
class GraphMetrics {
    @Bean
    fun statementCountMetric(
        contentTypeRepository: StatementRepository,
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
            contentTypeRepository.count(
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
