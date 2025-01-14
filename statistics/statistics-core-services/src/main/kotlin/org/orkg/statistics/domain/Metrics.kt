package org.orkg.statistics.domain

import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.statistics.output.StatisticsRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Metrics {
    //
    // Things
    //

    @Bean
    fun resourceCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "resource-count",
        description = "Number of resources in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Resource") }
    )

    @Bean
    fun predicateCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "predicate-count",
        description = "Number of predicates in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Predicate") }
    )

    @Bean
    fun literalCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "literal-count",
        description = "Number of literals in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Literal") }
    )

    @Bean
    fun classCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "class-count",
        description = "Number of classes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Class") }
    )

    @Bean
    fun statementCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "statement-count",
        description = "Number of statements in the graph.",
        group = "things",
        supplier = { statisticsRepository.countRelations("RELATED") }
    )

    @Bean
    fun orphansCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "orphan-count",
        description = "Number of orphaned nodes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Thing") }
    )

    @Bean
    fun orphanResourceCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "orphan-resource-count",
        description = "Number of orphan resources in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Resource") }
    )

    @Bean
    fun orphanPredicateCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "orphan-predicate-count",
        description = "Number of orphan predicates in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Predicate") }
    )

    @Bean
    fun orphanLiteralCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "orphan-literal-count",
        description = "Number of orphan literals in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Literal") }
    )

    @Bean
    fun orphanClassCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "orphan-class-count",
        description = "Number of orphan classes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Class") }
    )

    @Bean
    fun unusedResourceCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "unused-resource-count",
        description = "Number of unused resources in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Resource") }
    )

    @Bean
    fun unusedPredicateCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "unused-predicate-count",
        description = "Number of unused predicates in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Predicate") }
    )

    @Bean
    fun unusedLiteralCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "unused-literal-count",
        description = "Number of unused literals in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Literal") }
    )

    @Bean
    fun unusedClassCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "unused-class-count",
        description = "Number of unused classes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Class") }
    )

    //
    // Content-Types
    //

    @Bean
    fun paperCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "paper-count",
        description = "Number of papers in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("Paper") }
    )

    @Bean
    fun contributionCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "contribution-count",
        description = "Number of contributions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("Contribution") }
    )

    @Bean
    fun comparisonCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "comparison-count",
        description = "Number of comparisons in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("Comparison") }
    )

    @Bean
    fun publishedComparisonVersionCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "published-comparison-version-count",
        description = "Number of individual published comparison versions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("ComparisonPublished") }
    )

    @Bean
    fun visualizationCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "visualization-count",
        description = "Number of visualizations in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("Visualization") }
    )

    @Bean
    fun literatureListCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "literature-list-count",
        description = "Number of literature lists in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("LiteratureList") }
    )

    @Bean
    fun publishedLiteratureListVersionCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "published-literature-list-version-count",
        description = "Number of individual published literature list versions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("LiteratureListPublished") }
    )

    @Bean
    fun smartReviewCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "smart-review-count",
        description = "Number of smart reviews in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("SmartReview") }
    )

    @Bean
    fun publishedSmartReviewVersionCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "published-smart-review-version-count",
        description = "Number of individual published smart review versions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("SmartReviewPublished") }
    )

    @Bean
    fun templateCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "template-count",
        description = "Number of templates in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("NodeShape") }
    )

    @Bean
    fun researchFieldCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "research-field-count",
        description = "Number of research fields in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("ResearchField") }
    )

    @Bean
    fun problemCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "problem-count",
        description = "Number of problems in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("Problem") }
    )

    @Bean
    fun benchmarkCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "benchmark-count",
        description = "Number of benchmarks in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("C14022") }
    )

    @Bean
    fun rosettaStoneTemplateVersionCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "rosetta-stone-template-count",
        description = "Number of rosetta stone templates in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("RosettaNodeShape") }
    )

    @Bean
    fun rosettaStoneStatementVersionCountMetric(statisticsRepository: StatisticsRepository): Metric = CachedMetric(
        name = "rosetta-stone-statement-version-count",
        description = "Number of individual rosetta stone statement versions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("RosettaStoneStatement") }
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
}
