package org.orkg

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteratureListSnapshotV1
import org.orkg.contenttypes.domain.PublishedContentType
import org.orkg.contenttypes.domain.SnapshotIdGenerator
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.orkg.contenttypes.output.LiteratureListSnapshotRepository
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
@Profile("literatureListSnapshotMigration")
class LiteratureListSnapshotMigrationRunner(
    private val literatureListPublishedRepository: LiteratureListPublishedRepository,
    private val literatureListSnapshotRepository: LiteratureListSnapshotRepository,
    private val snapshotIdGenerator: SnapshotIdGenerator,
    private val resourceRepository: ResourceRepository,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments) {
        logger.info("Starting literature list snapshot migration...")

        if (literatureListSnapshotRepository.count() > 0) {
            logger.info("Skipping literature list snapshot migration, because database already contains entries")
            return
        }

        forEachResource(setOf(Classes.literatureListPublished)) { resource ->
            literatureListPublishedRepository.findById(resource.id).ifPresentOrElse(
                { migrateLiteratureListSnapshot(it, resource) },
                { logger.warn("Could not find table for published literature list ${resource.id}") },
            )
        }

        logger.info("Literature list snapshot migration complete")
    }

    private fun migrateLiteratureListSnapshot(literatureListPublished: PublishedContentType, resource: Resource) {
        try {
            literatureListSnapshotRepository.save(
                LiteratureListSnapshotV1(
                    id = snapshotIdGenerator.nextIdentity(),
                    createdBy = resource.createdBy,
                    createdAt = resource.createdAt,
                    resourceId = literatureListPublished.id,
                    rootId = literatureListPublished.rootId,
                    subgraph = literatureListPublished.subgraph,
                ),
            )
        } catch (e: Throwable) {
            logger.error("Failed to migrate snapshot for literature list ${literatureListPublished.id}", e)
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
