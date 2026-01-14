package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.AuthorStatisticsUseCases
import org.orkg.contenttypes.output.AuthorStatisticsRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
@TransactionalOnNeo4j(readOnly = true)
class AuthorStatisticsService(
    private val authorStatisticsRepository: AuthorStatisticsRepository,
) : AuthorStatisticsUseCases {
    override fun findAllByResearchProblemId(
        pageable: Pageable,
        researchProblem: ThingId,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
    ): Page<AuthorRecord> =
        authorStatisticsRepository.findAllByResearchProblemId(
            pageable = pageable,
            researchProblem = researchProblem,
            after = after,
            before = before,
        )
}
