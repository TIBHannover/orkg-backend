package org.orkg.contenttypes.domain

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.springframework.stereotype.Service

@Service
class PublishingService(
    private val doiService: DoiService,
    private val resourceRepository: ResourceRepository,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases
) {
    fun publish(command: PublishCommand): DOI {
        val resource = resourceRepository.findById(command.id)
            .orElseThrow { ResourceNotFound.withId(command.id) }
        if (!resource.hasPublishableClasses()) {
            throw UnpublishableThing(command.id)
        }
        val snapshotId = command.snapshotCreator.createSnapshot()
        val doi = doiService.register(
            DoiService.RegisterCommand(
                suffix = snapshotId.value,
                title = command.title,
                subject = command.subject,
                description = command.description,
                url = command.url,
                creators = command.creators,
                resourceType = command.resourceType.value,
                resourceTypeGeneral = "Dataset",
                relatedIdentifiers = command.relatedIdentifiers
            )
        )
        statementService.create(
            userId = command.contributorId,
            subject = snapshotId,
            predicate = Predicates.hasDOI,
            `object` = literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = doi.value
                )
            )
        )
        return doi
    }

    data class PublishCommand(
        val id: ThingId,
        val title: String,
        val contributorId: ContributorId,
        val subject: String,
        val description: String,
        val url: URI,
        val creators: List<Author>,
        val resourceType: ThingId,
        val relatedIdentifiers: List<String>,
        val snapshotCreator: SnapshotCreator
    )

    fun interface SnapshotCreator {
        fun createSnapshot(): ThingId
    }
}
