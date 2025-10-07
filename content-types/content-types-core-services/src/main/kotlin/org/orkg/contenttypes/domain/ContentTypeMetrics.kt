package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.ContentTypeRepository
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.contenttypes.output.VisualizationRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resources
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.orkg.statistics.domain.CachedMetric
import org.orkg.statistics.domain.Metric
import org.orkg.statistics.domain.MultiValueParameterSpec
import org.orkg.statistics.domain.ParameterMap
import org.orkg.statistics.domain.SingleValueParameterSpec
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val researchFieldParameter = SingleValueParameterSpec(
    name = "Research field id filter",
    description = "Filter for research field id.",
    type = ThingId::class,
    parser = ::ThingId
)

private val includeSubfieldsParameter = SingleValueParameterSpec(
    name = "Include subfields",
    description = "Whether to include subfields when filtering by research field id.",
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

private val sdgParameter = SingleValueParameterSpec(
    name = "Sustainable Development Goal filter",
    description = "Filter for a Sustainable Development Goal.",
    type = ThingId::class,
    values = Resources.sustainableDevelopmentGoals,
    parser = ::ThingId
)

private val resourceParameters = mapOf(
    "observatory_id" to observatoryIdParameter,
    "organization_id" to organizationIdParameter,
    "created_by" to createdByParameter,
    "created_at_start" to createdAtStartParameter,
    "created_at_end" to createdAtEndParameter,
    "visibility" to visibilityParameter,
)

private val sharedContentTypeParameters = resourceParameters + mapOf(
    "research_field" to researchFieldParameter,
    "include_subfields" to includeSubfieldsParameter,
)

private val publishedParameter = SingleValueParameterSpec(
    name = "Published filter",
    description = "Filter for publication state.",
    type = Boolean::class,
    parser = { it.toBoolean() }
)

private val publishableContentTypeParameters = sharedContentTypeParameters + mapOf(
    "published" to publishedParameter
)

private val verifiedParameter = SingleValueParameterSpec(
    name = "Verified filter",
    description = "Filter for verified state.",
    type = Boolean::class,
    parser = { it.toBoolean() }
)

private val researchProblemParameter = SingleValueParameterSpec(
    name = "Research problem filter",
    description = "Filter for research problem id.",
    type = ThingId::class,
    parser = ::ThingId
)

private val contentTypeClassParameter = MultiValueParameterSpec(
    name = "Content-Type class filter",
    description = "Filter for one or more content-type classes. If absent, all content-type classes are included.",
    type = ContentTypeClass::class,
    values = ContentTypeClass.entries,
    parser = ContentTypeClass::valueOf
)

@Configuration
class ContentTypeMetrics {
    @Bean
    fun contentTypeCountMetric(
        contentTypeRepository: ContentTypeRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "content-type-count",
        description = "Number of content-types in the graph. Content types include papers, comparisons, visualizations, templates, literature lists and smart reviews.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters + mapOf(
            "classes" to contentTypeClassParameter,
            "sdg" to sdgParameter
        ),
        supplier = { parameters ->
            contentTypeRepository.count(
                classes = parameters[contentTypeClassParameter].orEmpty().ifEmpty { ContentTypeClass.entries }.toSet(),
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                visibility = parameters[visibilityParameter],
                sustainableDevelopmentGoal = parameters[sdgParameter],
            )
        }
    )

    @Bean
    fun paperCountMetric(
        paperRepository: PaperRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "paper-count",
        description = "Number of papers in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters + mapOf(
            "verified" to verifiedParameter,
            "sdg" to sdgParameter,
            "research_problem" to researchProblemParameter,
        ),
        supplier = { parameters ->
            paperRepository.count(
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                visibility = parameters[visibilityParameter],
                sustainableDevelopmentGoal = parameters[sdgParameter],
                verified = parameters[verifiedParameter],
                researchProblem = parameters[researchProblemParameter],
            )
        }
    )

    @Bean
    fun comparisonCountMetric(
        comparisonRepository: ComparisonRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "comparison-count",
        description = "Number of comparisons in the graph.",
        group = "content-types",
        parameterSpecs = publishableContentTypeParameters + mapOf(
            "sdg" to sdgParameter
        ),
        supplier = { parameters ->
            comparisonRepository.count(
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                visibility = parameters[visibilityParameter],
                sustainableDevelopmentGoal = parameters[sdgParameter],
                published = parameters[publishedParameter],
            )
        }
    )

    @Bean
    fun visualizationCountMetric(
        visualizationRepository: VisualizationRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "visualization-count",
        description = "Number of visualizations in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            visualizationRepository.count(
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                visibility = parameters[visibilityParameter],
            )
        }
    )

    @Bean
    fun literatureListCountMetric(
        literatureListRepository: LiteratureListRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "literature-list-count",
        description = "Number of literature lists in the graph.",
        group = "content-types",
        parameterSpecs = publishableContentTypeParameters + mapOf(
            "sdg" to sdgParameter
        ),
        supplier = { parameters ->
            literatureListRepository.count(
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                visibility = parameters[visibilityParameter],
                sustainableDevelopmentGoal = parameters[sdgParameter],
                published = parameters[publishedParameter],
            )
        }
    )

    @Bean
    fun smartReviewCountMetric(
        smartReviewRepository: SmartReviewRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "smart-review-count",
        description = "Number of smart reviews in the graph.",
        group = "content-types",
        parameterSpecs = publishableContentTypeParameters + mapOf(
            "sdg" to sdgParameter
        ),
        supplier = { parameters ->
            smartReviewRepository.count(
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                visibility = parameters[visibilityParameter],
                sustainableDevelopmentGoal = parameters[sdgParameter],
                published = parameters[publishedParameter],
            )
        }
    )

    @Bean
    fun templateCountMetric(
        templateRepository: TemplateRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "template-count",
        description = "Number of templates in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            templateRepository.count(
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                organizationId = parameters[organizationIdParameter],
                createdBy = parameters[createdByParameter],
                createdAtStart = parameters[createdAtStartParameter],
                createdAtEnd = parameters[createdAtEndParameter],
                visibility = parameters[visibilityParameter],
            )
        }
    )

    @Bean
    fun rosettaStoneTemplateCountMetric(
        resourceRepository: ResourceRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "rosetta-stone-template-count",
        description = "Number of rosetta stone templates in the graph.",
        group = "content-types",
        parameterSpecs = resourceParameters,
        supplier = resourceRepositoryBasedParameterizedCountSupplier(
            resourceRepository = resourceRepository,
            classes = setOf(Classes.rosettaNodeShape)
        )
    )

    @Bean
    fun rosettaStoneStatementCountMetric(
        resourceRepository: ResourceRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "rosetta-stone-statement-count",
        description = "Number of rosetta stone statements in the graph.",
        group = "content-types",
        parameterSpecs = resourceParameters,
        supplier = resourceRepositoryBasedParameterizedCountSupplier(
            resourceRepository = resourceRepository,
            classes = setOf(Classes.rosettaStoneStatement, Classes.latestVersion)
        )
    )

    @Bean
    fun rosettaStoneStatementVersionCountMetric(
        resourceRepository: ResourceRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "rosetta-stone-statement-version-count",
        description = "Number of individual rosetta stone statement versions in the graph.",
        group = "content-types",
        parameterSpecs = resourceParameters,
        supplier = resourceRepositoryBasedParameterizedCountSupplier(
            resourceRepository = resourceRepository,
            classes = setOf(Classes.rosettaStoneStatement)
        )
    )

    private fun resourceRepositoryBasedParameterizedCountSupplier(
        resourceRepository: ResourceRepository,
        classes: Set<ThingId>,
    ) = { parameters: ParameterMap ->
        resourceRepository.count(
            includeClasses = classes,
            observatoryId = parameters[observatoryIdParameter],
            organizationId = parameters[organizationIdParameter],
            createdBy = parameters[createdByParameter],
            createdAtStart = parameters[createdAtStartParameter],
            createdAtEnd = parameters[createdAtEndParameter],
            visibility = parameters[visibilityParameter],
        )
    }
}
