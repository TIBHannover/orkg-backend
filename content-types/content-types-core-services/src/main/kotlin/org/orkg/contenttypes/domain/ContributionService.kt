package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ContributionService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) : ContributionUseCases {
    override fun findById(id: ThingId): Optional<Contribution> =
        resourceRepository.findById(id)
            .filter { Classes.contribution in it.classes }
            .map { it.toContribution() }

    override fun findAll(pageable: Pageable): Page<Contribution> =
        resourceRepository.findAllByClass(Classes.contribution, pageable)
            .pmap { it.toContribution() }

    private fun Resource.toContribution(): Contribution {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .withoutObjectsWithBlankLabels()
        return Contribution(
            id = this@toContribution.id,
            label = this@toContribution.label,
            classes = this@toContribution.classes,
            properties = statements.toContributionSubGraph(),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = this@toContribution.visibility,
            unlistedBy = unlistedBy
        )
    }
}
