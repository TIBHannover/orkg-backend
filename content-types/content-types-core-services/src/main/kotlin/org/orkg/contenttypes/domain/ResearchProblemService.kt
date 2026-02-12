package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
class ResearchProblemService(
    private val resourceRepository: ResourceRepository,
    private val repository: ResearchProblemRepository,
) : ResearchProblemUseCases {
    override fun findById(id: ThingId): Optional<Resource> =
        resourceRepository.findById(id)
            .filter { Classes.problem in it.classes }

    override fun findAll(
        pageable: Pageable,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        addressedByObservatory: ObservatoryId?,
        addressedByOrganization: OrganizationId?,
    ): Page<Resource> =
        repository.findAll(
            pageable = pageable,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            addressedByObservatory = addressedByObservatory,
            addressedByOrganization = addressedByOrganization,
        )
}
