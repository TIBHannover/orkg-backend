package org.orkg.statistics.domain

import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.statistics.output.StatisticsRepository
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Metrics {
    //
    // Things
    //

    @Bean
    fun resourceCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "resource-count",
        description = "Number of resources in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Resource") }
    )

    @Bean
    fun predicateCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "predicate-count",
        description = "Number of predicates in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Predicate") }
    )

    @Bean
    fun literalCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "literal-count",
        description = "Number of literals in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Literal") }
    )

    @Bean
    fun classCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "class-count",
        description = "Number of classes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countNodes("Class") }
    )

    @Bean
    fun orphansCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "orphan-count",
        description = "Number of orphaned nodes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Thing") }
    )

    @Bean
    fun orphanResourceCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "orphan-resource-count",
        description = "Number of orphan resources in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Resource") }
    )

    @Bean
    fun orphanPredicateCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "orphan-predicate-count",
        description = "Number of orphan predicates in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Predicate") }
    )

    @Bean
    fun orphanLiteralCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "orphan-literal-count",
        description = "Number of orphan literals in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Literal") }
    )

    @Bean
    fun orphanClassCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "orphan-class-count",
        description = "Number of orphan classes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countOrphanNodes("Class") }
    )

    @Bean
    fun unusedResourceCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "unused-resource-count",
        description = "Number of unused resources in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Resource") }
    )

    @Bean
    fun unusedPredicateCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "unused-predicate-count",
        description = "Number of unused predicates in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Predicate") }
    )

    @Bean
    fun unusedLiteralCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "unused-literal-count",
        description = "Number of unused literals in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Literal") }
    )

    @Bean
    fun unusedClassCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "unused-class-count",
        description = "Number of unused classes in the graph.",
        group = "things",
        supplier = { statisticsRepository.countUnusedNodes("Class") }
    )

    //
    // Content-Types
    //

    @Bean
    fun contributionCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "contribution-count",
        description = "Number of contributions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("Contribution") }
    )

    @Bean
    fun publishedComparisonVersionCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "published-comparison-version-count",
        description = "Number of individual published comparison versions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("ComparisonPublished") }
    )

    @Bean
    fun publishedLiteratureListVersionCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "published-literature-list-version-count",
        description = "Number of individual published literature list versions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("LiteratureListPublished") }
    )

    @Bean
    fun publishedSmartReviewVersionCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "published-smart-review-version-count",
        description = "Number of individual published smart review versions in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("SmartReviewPublished") }
    )

    @Bean
    fun researchFieldCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "research-field-count",
        description = "Number of research fields in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("ResearchField") }
    )

    @Bean
    fun problemCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "problem-count",
        description = "Number of problems in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("Problem") }
    )

    @Bean
    fun benchmarkCountMetric(
        statisticsRepository: StatisticsRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "benchmark-count",
        description = "Number of benchmarks in the graph.",
        group = "content-types",
        supplier = { statisticsRepository.countNodes("C14022") }
    )

    //
    // Community
    //

    @Bean
    fun contributorsCountMetric(
        contributorRepository: ContributorRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "contributors-count",
        description = "Number of contributors.",
        group = "community",
        supplier = { contributorRepository.count() }
    )

    @Bean
    fun organizationCountMetric(
        organizationRepository: OrganizationRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "organization-count",
        description = "Number of organizations.",
        group = "community",
        supplier = { organizationRepository.count() }
    )

    @Bean
    fun observatoryCountMetric(
        observatoryRepository: ObservatoryRepository,
        cacheManager: CacheManager?,
    ): Metric = CachedMetric.create(
        cacheManager = cacheManager,
        name = "observatory-count",
        description = "Number of observatories.",
        group = "community",
        supplier = { observatoryRepository.count() }
    )
}
