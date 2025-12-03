package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ResearchFieldUseCases
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class ResearchFieldService(
    private val resourceRepository: ResourceRepository,
    private val repository: ResearchFieldRepository,
) : ResearchFieldUseCases {
    override fun findById(id: ThingId): Optional<Resource> =
        resourceRepository.findById(id)
            .filter { Classes.researchField in it.classes }

    override fun findAll(
        pageable: Pageable,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchProblem: ThingId?,
        includeSubproblems: Boolean,
    ): Page<Resource> =
        repository.findAll(
            pageable = pageable,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchProblem = researchProblem,
            includeSubproblems = includeSubproblems,
        )
}
