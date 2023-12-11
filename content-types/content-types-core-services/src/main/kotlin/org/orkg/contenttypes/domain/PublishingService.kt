package org.orkg.contenttypes.domain

import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Literals
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
    private val literalService: LiteralUseCases,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    fun publish(command: PublishCommand): DOI {
        val resource = resourceRepository.findById(command.id)
            .orElseThrow { ResourceNotFound.withId(command.id) }
        if (!resource.hasPublishableClasses()) {
            throw UnpublishableThing(command.id)
        }
        val hasDoiStatements = statementService.findAllBySubjectAndPredicate(command.id, Predicates.hasDOI, PageRequests.SINGLE)
        if (!hasDoiStatements.isEmpty) {
            throw DoiAlreadyRegistered(command.id)
        }
        val doi = doiService.register(
            DoiService.RegisterCommand(
                suffix = command.id.value,
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
            subject = command.id,
            predicate = Predicates.hasDOI,
            `object` = literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = doi.value
                )
            )
        )
        val now = OffsetDateTime.now(clock)
        statementService.create(
            userId = command.contributorId,
            subject = command.id,
            predicate = Predicates.yearPublished,
            `object` = literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = now.year.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        )
        statementService.create(
            userId = command.contributorId,
            subject = command.id,
            predicate = Predicates.monthPublished,
            `object` = literalService.create(
                CreateCommand(
                    contributorId = command.contributorId,
                    label = now.monthValue.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
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
        val relatedIdentifiers: List<String>
    )
}
