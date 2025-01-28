package org.orkg.contenttypes.domain

import org.orkg.common.ObservatoryId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.contenttypes.output.VisualizationRepository
import org.orkg.statistics.domain.Metric
import org.orkg.statistics.domain.ParameterSpec
import org.orkg.statistics.domain.SimpleMetric
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val researchFieldParameter = ParameterSpec(
    name = "Research field id filter",
    description = "Filter for research field id.",
    type = ThingId::class,
    parser = ::ThingId
)

private val includeSubfieldsParameter = ParameterSpec(
    name = "Include subfields",
    description = "Whether to include subfields when filtering by research field id.",
    type = Boolean::class,
    parser = { it.toBoolean() }
)

private val observatoryIdParameter = ParameterSpec(
    name = "Observatory id filter",
    description = "Filter for observatory id.",
    type = ObservatoryId::class,
    parser = ::ObservatoryId
)

private val sharedContentTypeParameters = mapOf(
    "research_field" to researchFieldParameter,
    "include_subfields" to includeSubfieldsParameter,
    "observatory_id" to observatoryIdParameter,
)

@Configuration
class ContentTypeMetrics {
    @Bean
    fun paperCountMetric(paperRepository: PaperRepository): Metric = SimpleMetric(
        name = "paper-count",
        description = "Number of papers in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            paperRepository.findAll(
                pageable = PageRequests.SINGLE,
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter]
            ).totalElements
        }
    )

    @Bean
    fun comparisonCountMetric(comparisonRepository: ComparisonRepository): Metric = SimpleMetric(
        name = "comparison-count",
        description = "Number of comparisons in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            comparisonRepository.findAll(
                pageable = PageRequests.SINGLE,
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                published = false
            ).totalElements
        }
    )

    @Bean
    fun visualizationCountMetric(visualizationRepository: VisualizationRepository): Metric = SimpleMetric(
        name = "visualization-count",
        description = "Number of visualizations in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            visualizationRepository.findAll(
                pageable = PageRequests.SINGLE,
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter]
            ).totalElements
        }
    )

    @Bean
    fun literatureListCountMetric(literatureListRepository: LiteratureListRepository): Metric = SimpleMetric(
        name = "literature-list-count",
        description = "Number of literature lists in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            literatureListRepository.findAll(
                pageable = PageRequests.SINGLE,
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                published = false
            ).totalElements
        }
    )

    @Bean
    fun smartReviewCountMetric(smartReviewRepository: SmartReviewRepository): Metric = SimpleMetric(
        name = "smart-review-count",
        description = "Number of smart reviews in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            smartReviewRepository.findAll(
                pageable = PageRequests.SINGLE,
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter],
                published = false
            ).totalElements
        }
    )

    @Bean
    fun templateCountMetric(templateRepository: TemplateRepository): Metric = SimpleMetric(
        name = "template-count",
        description = "Number of templates in the graph.",
        group = "content-types",
        parameterSpecs = sharedContentTypeParameters,
        supplier = { parameters ->
            templateRepository.findAll(
                pageable = PageRequests.SINGLE,
                researchField = parameters[researchFieldParameter],
                includeSubfields = parameters[includeSubfieldsParameter] ?: false,
                observatoryId = parameters[observatoryIdParameter]
            ).totalElements
        }
    )
}
