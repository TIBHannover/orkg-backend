package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.api.AuthorRepresentation
import eu.tib.orkg.prototype.contenttypes.api.LabeledObjectRepresentation
import eu.tib.orkg.prototype.contenttypes.api.PaperRepresentation
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contenttypes.api.PublicationInfoRepresentation
import eu.tib.orkg.prototype.contenttypes.api.VisibilityFilter
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.OffsetDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

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

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<PaperRepresentation> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> resourceRepository.findAllListedPapers(pageable)
            VisibilityFilter.NON_FEATURED -> resourceRepository.findAllPapersByVisibility(Visibility.DEFAULT, pageable)
            VisibilityFilter.UNLISTED -> resourceRepository.findAllPapersByVisibility(Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> resourceRepository.findAllPapersByVisibility(Visibility.FEATURED, pageable)
            VisibilityFilter.DELETED -> resourceRepository.findAllPapersByVisibility(Visibility.DELETED, pageable)
        }.pmap { it.toPaperRepresentation() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<PaperRepresentation> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.paper, contributorId, pageable)
            .pmap { it.toPaperRepresentation() }

    override fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        resourceRepository.findPaperByResourceId(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { PaperNotFound(id) }

    private fun Resource.toPaperRepresentation(): PaperRepresentation {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .filter { it.`object`.label.isNotBlank() }
        return object : PaperRepresentation {
            override val id: ThingId = this@toPaperRepresentation.id
            override val title: String = this@toPaperRepresentation.label
            override val researchFields: List<LabeledObjectRepresentation> = statements.wherePredicate(Predicates.hasResearchField)
                .objectIdsWithLabel()
                .sortedBy { it.id }
            override val identifiers: Map<String, String> = statements.mapIdentifiers(Identifiers.paper)
            override val publicationInfo = object : PublicationInfoRepresentation {
                override val publishedMonth: Int? = statements.wherePredicate(Predicates.monthPublished).firstObjectLabel()?.toIntOrNull()
                override val publishedYear: Long? = statements.wherePredicate(Predicates.yearPublished).firstObjectLabel()?.toLongOrNull()
                override val publishedIn: String? = statements.wherePredicate(Predicates.hasVenue).firstObjectLabel()
                override val url: String? = statements.wherePredicate(Predicates.hasURL).firstObjectLabel()
            }
            override val authors: List<AuthorRepresentation> = statements.wherePredicate(Predicates.hasAuthor).objects()
                .filter { it is Resource || it is Literal }
                .pmap { it.toAuthorRepresentation() }
            override val contributions: List<LabeledObjectRepresentation> = statements.wherePredicate(Predicates.hasContribution)
                .objectIdsWithLabel()
                .sortedBy { it.id }
            override val observatories: List<ObservatoryId> = listOf(observatoryId)
            override val organizations: List<OrganizationId> = listOf(organizationId)
            override val extractionMethod: ExtractionMethod = this@toPaperRepresentation.extractionMethod
            override val createdAt: OffsetDateTime = this@toPaperRepresentation.createdAt
            override val createdBy: ContributorId = this@toPaperRepresentation.createdBy
            override val verified: Boolean = this@toPaperRepresentation.verified ?: false
            override val visibility: Visibility = this@toPaperRepresentation.visibility
        }
    }

    private fun Thing.toAuthorRepresentation(): AuthorRepresentation {
        return when (this) {
            is Resource -> toAuthorRepresentation()
            is Literal -> toAuthorRepresentation()
            else -> throw IllegalStateException("""Cannot convert "$thingId" to author. This is a bug!""")
        }
    }

    private fun Resource.toAuthorRepresentation(): AuthorRepresentation {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .filter { it.`object`.label.isNotBlank() }
        return object : AuthorRepresentation {
            override val id: ThingId = this@toAuthorRepresentation.id
            override val name: String = label
            override val identifiers: Map<String, String> = statements.mapIdentifiers(Identifiers.author)
            override val homepage: String? = statements.wherePredicate(Predicates.hasWebsite).firstObjectLabel()
        }
    }

    private fun Literal.toAuthorRepresentation() = object : AuthorRepresentation {
        override val id: ThingId? = null
        override val name: String = label
        override val identifiers: Map<String, String> = emptyMap()
        override val homepage: String? = null
    }
}
