package org.orkg.statistics.domain

import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.graph.output.StatsRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Metrics {
    //
    // Things
    //

    @Bean
    fun resourceCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "resource-count",
        description = "Number of resources in the graph.",
        group = "things",
        supplier = { statsRepository.findNodeCountForLabel("Resource") }
    )

    @Bean
    fun predicateCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "predicate-count",
        description = "Number of predicates in the graph.",
        group = "things",
        supplier = { statsRepository.findNodeCountForLabel("Predicate") }
    )

    @Bean
    fun literalCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "literal-count",
        description = "Number of literals in the graph.",
        group = "things",
        supplier = { statsRepository.findNodeCountForLabel("Literal") }
    )

    @Bean
    fun classCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "class-count",
        description = "Number of classes in the graph.",
        group = "things",
        supplier = { statsRepository.findNodeCountForLabel("Class") }
    )

    @Bean
    fun statementCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "statement-count",
        description = "Number of statements in the graph.",
        group = "things",
        supplier = { (statsRepository.getGraphMetaData().first()["relTypesCount"] as Map<*, *>)["RELATED"] as Long }
    )

    @Bean
    fun orphansCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "orphan-count",
        description = "Number of orphaned nodes in the graph.",
        group = "things",
        supplier = statsRepository::getOrphanedNodesCount
    )

    //
    // Content-Types
    //

    @Bean
    fun paperCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "paper-count",
        description = "Number of papers in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("Paper") }
    )

    @Bean
    fun contributionCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "contribution-count",
        description = "Number of contributions in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("Contribution") }
    )

    @Bean
    fun comparisonCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "comparison-count",
        description = "Number of comparisons in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("Comparison") }
    )

    @Bean
    fun visualizationCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "visualization-count",
        description = "Number of visualizations in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("Visualization") }
    )

    @Bean
    fun literatureListCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "literature-list-count",
        description = "Number of literature lists in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("LiteratureList") }
    )

    @Bean
    fun smartReviewCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "smart-review-count",
        description = "Number of smart reviews lists in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("SmartReview") }
    )

    @Bean
    fun templateCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "template-count",
        description = "Number of templates lists in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("NodeShape") }
    )

    @Bean
    fun researchFieldCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "research-field-count",
        description = "Number of research fields in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("ResearchField") }
    )

    @Bean
    fun problemCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "problem-count",
        description = "Number of problems in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("Problem") }
    )

    @Bean
    fun benchmarkCountMetric(statsRepository: StatsRepository): Metric = CachedMetric(
        name = "benchmark-count",
        description = "Number of benchmarks in the graph.",
        group = "content-types",
        supplier = { statsRepository.findNodeCountForLabel("C14022") }
    )

    //
    // Community
    //

    @Bean
    fun contributorsCountMetric(contributorRepository: ContributorRepository): Metric = CachedMetric(
        name = "contributors-count",
        description = "Number of contributors.",
        group = "community",
        supplier = contributorRepository::countActiveUsers
    )

    @Bean
    fun organizationCountMetric(organizationRepository: OrganizationRepository): Metric = CachedMetric(
        name = "organization-count",
        description = "Number of organizations.",
        group = "community",
        supplier = organizationRepository::count
    )

    @Bean
    fun observatoryCountMetric(observatoryRepository: ObservatoryRepository): Metric = CachedMetric(
        name = "observatory-count",
        description = "Number of observatories.",
        group = "community",
        supplier = observatoryRepository::count
    )

    private fun StatsRepository.findNodeCountForLabel(label: String): Long =
        (getGraphMetaData().first()["labels"] as Map<*, *>)[label] as Long
}
