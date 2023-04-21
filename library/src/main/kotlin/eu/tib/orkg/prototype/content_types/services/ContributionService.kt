package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.content_types.api.ContributionRepresentation
import eu.tib.orkg.prototype.content_types.api.ContributionUseCases
import eu.tib.orkg.prototype.content_types.application.ContributionNotFound
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ContributionService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) : ContributionUseCases {
    override fun findById(id: ThingId): ContributionRepresentation =
        resourceRepository.findByResourceId(id)
            .filter { Classes.contribution in it.classes }
            .map { it.toContributionRepresentation() }
            .orElseThrow { ContributionNotFound(id) }

    override fun findAll(pageable: Pageable): Page<ContributionRepresentation> =
        resourceRepository.findAllByClass(Classes.contribution, pageable)
            .pmap { it.toContributionRepresentation() }

    private fun Resource.toContributionRepresentation(): ContributionRepresentation {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .filter { it.`object`.label.isNotBlank() }
        return object : ContributionRepresentation {
            override val id: ThingId = this@toContributionRepresentation.id
            override val label: String = this@toContributionRepresentation.label
            override val properties: Map<ThingId, List<ThingId>> = statements
                .groupBy { it.predicate.id }
                .mapValues {
                    it.value.map { statement -> statement.`object`.thingId }
                }
            override val visibility: Visibility = this@toContributionRepresentation.visibility
        }
    }
}
