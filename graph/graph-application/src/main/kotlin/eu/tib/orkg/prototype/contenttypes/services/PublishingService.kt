package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.application.DoiAlreadyRegistered
import eu.tib.orkg.prototype.contenttypes.application.UnpublishableThing
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.spi.DoiService
import eu.tib.orkg.prototype.identifiers.domain.DOI
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.publishableClasses
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
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
        if (resource.classes.intersect(publishableClasses).isEmpty()) {
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
            subject = command.id,
            predicate = Predicates.hasDOI,
            `object` = literalService.create(label = doi.value).id
        )
        val now = OffsetDateTime.now(clock)
        statementService.create(
            subject = command.id,
            predicate = Predicates.yearPublished,
            `object` = literalService.create(
                label = now.year.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            ).id
        )
        statementService.create(
            subject = command.id,
            predicate = Predicates.monthPublished,
            `object` = literalService.create(
                label = now.monthValue.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            ).id
        )
        return doi
    }

    data class PublishCommand(
        val id: ThingId,
        val title: String,
        val subject: String,
        val description: String,
        val url: URI,
        val creators: List<Author>,
        val resourceType: ThingId,
        val relatedIdentifiers: List<String>
    )
}
