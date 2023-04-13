package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.content_types.api.PaperRepresentation
import eu.tib.orkg.prototype.content_types.api.PaperUseCases
import eu.tib.orkg.prototype.content_types.api.PublicationInfoRepresentation
import eu.tib.orkg.prototype.content_types.application.PaperNotFound
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.OffsetDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val paperIdentifiers = mapOf(
    Predicates.hasDOI to "doi"
)

@Service
class PaperService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) : PaperUseCases {
    override fun findById(id: ThingId): PaperRepresentation =
        resourceRepository.findPaperByResourceId(id)
            .map { it.toPaperRepresentation() }
            .orElseThrow { PaperNotFound(id) }

    override fun findAll(pageable: Pageable): Page<PaperRepresentation> =
        resourceRepository.findAllByClass(Classes.paper, pageable)
            .pmap { it.toPaperRepresentation() }

    override fun findAllByDOI(doi: String, pageable: Pageable): Page<PaperRepresentation> =
        statementRepository.findAllPapersByDOI(doi, pageable)
            .pmap { it.toPaperRepresentation() }

    override fun findAllByTitle(title: String, pageable: Pageable): Page<PaperRepresentation> =
        resourceRepository.findAllByClassAndLabel(Classes.paper, title, pageable)
            .pmap { it.toPaperRepresentation() }

    private fun Resource.toPaperRepresentation(): PaperRepresentation {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .filter { it.`object`.label.isNotBlank() }
        val contributors = statementRepository.findAllContributorsByResourceId(id, PageRequests.ALL).content
        return object : PaperRepresentation {
            override val id: ThingId = this@toPaperRepresentation.id
            override val title: String = this@toPaperRepresentation.label
            override val researchField: List<ThingId> = statements.wherePredicate(Predicates.hasResearchField).objectIds().sortedBy { it.value }
            override val identifiers: Map<String, String> = statements
                .filter { it.predicate.id in paperIdentifiers.keys }
                .associate { paperIdentifiers[it.predicate.id]!! to it.`object`.label }
            override val publicationInfo = object : PublicationInfoRepresentation {
                override val publishedMonth: Int? = statements.wherePredicate(Predicates.monthPublished).firstObjectLabel()?.toIntOrNull()
                override val publishedYear: Long? = statements.wherePredicate(Predicates.yearPublished).firstObjectLabel()?.toLongOrNull()
                override val publishedIn: String? = statements.wherePredicate(Predicates.hasVenue).firstObjectLabel()
                override val url: String? = statements.wherePredicate(Predicates.hasURL).firstObjectLabel()
            }
            override val authors: List<ThingId> = statements.wherePredicate(Predicates.hasAuthor).objectIds().sortedBy { it.value }
            override val contributors: List<ContributorId> = contributors
            override val observatories: List<ObservatoryId> = listOf(observatoryId)
            override val organizations: List<OrganizationId> = listOf(organizationId)
            override val extractionMethod: ExtractionMethod = this@toPaperRepresentation.extractionMethod
            override val createdAt: OffsetDateTime = this@toPaperRepresentation.createdAt
            override val createdBy: ContributorId = this@toPaperRepresentation.createdBy
            override val featured: Boolean = this@toPaperRepresentation.featured ?: false
            override val unlisted: Boolean = this@toPaperRepresentation.unlisted ?: false
            override val verified: Boolean = this@toPaperRepresentation.verified ?: false
            override val deleted: Boolean = Classes.paperDeleted in classes
        }
    }
}
