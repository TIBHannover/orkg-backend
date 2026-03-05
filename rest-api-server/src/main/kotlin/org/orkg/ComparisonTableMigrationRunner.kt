package org.orkg

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.legacy.LegacyComparisonConfig
import org.orkg.contenttypes.domain.legacy.LegacyComparisonData
import org.orkg.contenttypes.domain.legacy.LegacyComparisonType
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.orkg.contenttypes.output.legacy.LegacyComparisonPublishedRepository
import org.orkg.contenttypes.output.legacy.LegacyComparisonTableRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.ResourceRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

private const val CHUNK_SIZE = 10_000

@Component
@Profile("comparisonTableMigrations")
class ComparisonTableMigrationRunner(
    private val legacyComparisonTableRepository: LegacyComparisonTableRepository,
    private val legacyComparisonPublishedRepository: LegacyComparisonPublishedRepository,
    private val comparisonTableRepository: ComparisonTableRepository,
    private val pathComparisonTableMigrator: PathComparisonTableMigrator,
    private val mergeComparisonTableMigrator: MergeComparisonTableMigrator,
    private val resourceRepository: ResourceRepository,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting comparison table migration...")

        if (comparisonTableRepository.count() > 0) {
            logger.info("Skipping comparison table migration, because database already contains entries")
            return
        }

        forEachResource(setOf(Classes.comparison)) {
            legacyComparisonTableRepository.findById(it.id).ifPresentOrElse(
                { migrateComparisonTable(it.id, it.config, it.data, false) },
                { logger.warn("Could not find table for comparison ${it.id}") },
            )
        }

        forEachResource(setOf(Classes.comparisonPublished)) {
            legacyComparisonPublishedRepository.findById(it.id).ifPresentOrElse(
                { migrateComparisonTable(it.id, it.config, it.data, true) },
                { logger.warn("Could not find table for published comparison ${it.id}") },
            )
        }

        logger.info("Comparison table migration complete")
    }

    private fun migrateComparisonTable(
        comparisonId: ThingId,
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
        parseTable: Boolean,
    ) {
        try {
            val table = when (legacyConfig.type) {
                LegacyComparisonType.PATH -> pathComparisonTableMigrator.parse(comparisonId, legacyConfig, legacyData, parseTable)
                LegacyComparisonType.MERGE -> mergeComparisonTableMigrator.parse(comparisonId, legacyConfig, legacyData, parseTable)
            }
            comparisonTableRepository.save(table)
        } catch (e: Throwable) {
            logger.error("Failed to migrate table for comparison $comparisonId", e)
        }
    }

    private fun forEachResource(includeClasses: Set<ThingId>, action: (Resource) -> Unit) {
        var page: Page<Resource>
        var pageNumber = 0
        do {
            page = resourceRepository.findAll(
                pageable = PageRequest.of(pageNumber++, CHUNK_SIZE),
                includeClasses = includeClasses,
            )
            page.forEach(action)
        }
        while (page.hasNext())
    }
}
