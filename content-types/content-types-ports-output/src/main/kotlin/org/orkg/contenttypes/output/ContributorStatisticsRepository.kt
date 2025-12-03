package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributorRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime

interface ContributorStatisticsRepository {
    fun findAll(
        pageable: Pageable,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
    ): Page<ContributorRecord>

    fun findAllByResearchFieldId(
        pageable: Pageable,
        researchField: ThingId,
        includeSubfields: Boolean = false,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
    ): Page<ContributorRecord>

    fun findAllByResearchProblemId(
        pageable: Pageable,
        researchProblem: ThingId,
        includeSubproblems: Boolean = false,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
    ): Page<ContributorRecord>
}
