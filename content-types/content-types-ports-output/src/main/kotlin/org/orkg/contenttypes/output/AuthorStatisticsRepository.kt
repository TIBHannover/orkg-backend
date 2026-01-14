package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.AuthorRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime

interface AuthorStatisticsRepository {
    fun findAllByResearchProblemId(
        pageable: Pageable,
        researchProblem: ThingId,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
    ): Page<AuthorRecord>
}
