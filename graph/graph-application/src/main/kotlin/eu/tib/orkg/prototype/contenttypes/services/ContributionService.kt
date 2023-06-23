package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.api.ContributionUseCases
import eu.tib.orkg.prototype.contenttypes.application.ContributionNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import eu.tib.orkg.prototype.contenttypes.domain.model.ContributionSubGraph
import eu.tib.orkg.prototype.contenttypes.domain.model.toContributionSubGraph
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
    override fun findById(id: ThingId): Contribution =
        resourceRepository.findById(id)
            .filter { Classes.contribution in it.classes }
            .map { it.toContribution() }
            .orElseThrow { ContributionNotFound(id) }

    override fun findAll(pageable: Pageable): Page<Contribution> =
        resourceRepository.findAllByClass(Classes.contribution, pageable)
            .pmap { it.toContribution() }

    private fun Resource.toContribution(): Contribution {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .withoutObjectsHavingBlankLabels()
        return Contribution(
            id = this@toContribution.id,
            label = this@toContribution.label,
            properties = statements.toContributionSubGraph(),
            visibility = this@toContribution.visibility
        )
    }
}
