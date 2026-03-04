package org.orkg

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.domain.SmartReviewSnapshotV1
import org.orkg.contenttypes.domain.SnapshotIdGenerator
import org.orkg.contenttypes.output.SmartReviewPublishedRepository
import org.orkg.contenttypes.output.SmartReviewSnapshotRepository
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
@Profile("smartReviewSnapshotMigration")
class SmartReviewSnapshotMigrationRunner(
    private val smartReviewPublishedRepository: SmartReviewPublishedRepository,
    private val smartReviewSnapshotRepository: SmartReviewSnapshotRepository,
    private val snapshotIdGenerator: SnapshotIdGenerator,
    private val resourceRepository: ResourceRepository,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting smart review snapshot migration...")

        if (smartReviewSnapshotRepository.count() > 0) {
            logger.info("Skipping smart review snapshot migration, because database already contains entries")
            return
        }

        forEachResource(setOf(Classes.smartReviewPublished)) { resource ->
            smartReviewPublishedRepository.findById(resource.id).ifPresentOrElse(
                { migrateSmartReviewSnapshot(it, resource) },
                { logger.warn("Could not find table for published smart review ${resource.id}") }
            )
        }

        logger.info("Smart review snapshot migration complete")
    }

    private fun migrateSmartReviewSnapshot(smartReviewPublished: PublishedContentType, resource: Resource) {
        try {
            smartReviewSnapshotRepository.save(
                SmartReviewSnapshotV1(
                    id = snapshotIdGenerator.nextIdentity(),
                    createdBy = resource.createdBy,
                    createdAt = resource.createdAt,
                    resourceId = smartReviewPublished.id,
                    rootId = smartReviewPublished.rootId,
                    subgraph = smartReviewPublished.subgraph,
                )
            )
        } catch (e: Throwable) {
            logger.error("Failed to migrate snapshot for smart review ${smartReviewPublished.id}", e)
        }
    }

    private fun forEachResource(includeClasses: Set<ThingId>, action: (Resource) -> Unit) {
        var page: Page<Resource>
        var pageNumber = 0
        do {
            page = resourceRepository.findAll(
                pageable = PageRequest.of(pageNumber++, CHUNK_SIZE),
                includeClasses = includeClasses
            )
            page.forEach(action)
        }
        while (page.hasNext())
    }
}
