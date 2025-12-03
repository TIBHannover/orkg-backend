package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContributorStatisticsUseCases
import org.orkg.contenttypes.output.ContributorStatisticsRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
@TransactionalOnNeo4j(readOnly = true)
class ContributorStatisticsService(
    private val contributorStatisticsRepository: ContributorStatisticsRepository,
) : ContributorStatisticsUseCases {
    override fun findAll(
        pageable: Pageable,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<ContributorRecord> =
        contributorStatisticsRepository.findAll(
            pageable = pageable,
            after = after,
            before = before,
        )

    override fun findAllByResearchFieldId(
        pageable: Pageable,
        researchField: ThingId,
        includeSubfields: Boolean,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<ContributorRecord> =
        contributorStatisticsRepository.findAllByResearchFieldId(
            pageable = pageable,
            researchField = researchField,
            includeSubfields = includeSubfields,
            after = after,
            before = before,
        )

    override fun findAllByResearchProblemId(
        pageable: Pageable,
        researchProblem: ThingId,
        includeSubproblems: Boolean,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<ContributorRecord> =
        contributorStatisticsRepository.findAllByResearchProblemId(
            pageable = pageable,
            researchProblem = researchProblem,
            includeSubproblems = includeSubproblems,
            after = after,
            before = before,
        )
}
