package org.orkg

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperSnapshotV1
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.domain.SnapshotIdGenerator
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.orkg.contenttypes.output.PaperSnapshotRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
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
@Profile("paperSnapshotMigration")
class PaperSnapshotMigrationRunner(
    private val paperPublishedRepository: PaperPublishedRepository,
    private val paperSnapshotRepository: PaperSnapshotRepository,
    private val snapshotIdGenerator: SnapshotIdGenerator,
    private val resourceRepository: ResourceRepository,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting paper snapshot migration...")

        if (paperSnapshotRepository.count() > 0) {
            logger.info("Skipping paper snapshot migration, because database already contains entries")
            return
        }

        forEachResource(setOf(Classes.paperVersion)) { resource ->
            paperPublishedRepository.findById(resource.id).ifPresentOrElse(
                { migratePaperSnapshot(it, resource) },
                { logger.warn("Could not find snapshot for published paper ${resource.id}") },
            )
        }

        logger.info("Paper snapshot migration complete")
    }

    private fun migratePaperSnapshot(statements: List<GeneralStatement>, resource: Resource) {
        try {
            paperSnapshotRepository.save(
                PaperSnapshotV1(
                    id = snapshotIdGenerator.nextIdentity(),
                    createdBy = resource.createdBy,
                    createdAt = resource.createdAt,
                    resourceId = resource.id,
                    subgraph = statements,
                ),
            )
        } catch (e: Throwable) {
            logger.error("Failed to migrate snapshot for paper ${resource.id}", e)
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
