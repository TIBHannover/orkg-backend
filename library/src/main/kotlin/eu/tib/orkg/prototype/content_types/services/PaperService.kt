package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.content_types.api.AuthorRepresentation
import eu.tib.orkg.prototype.content_types.api.PaperRepresentation
import eu.tib.orkg.prototype.content_types.api.PaperUseCases
import eu.tib.orkg.prototype.content_types.api.PublicationInfoRepresentation
import eu.tib.orkg.prototype.content_types.application.PaperNotFound
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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

    private fun Resource.toPaperRepresentation(): PaperRepresentation {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .filter { it.`object`.label.isNotBlank() }
        return object : PaperRepresentation {
            override val id: ThingId = this@toPaperRepresentation.id
            override val title: String = this@toPaperRepresentation.label
            override val researchFields: List<ThingId> = statements.wherePredicate(Predicates.hasResearchField).objectIds().sortedBy { it.value }
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
