package org.orkg.contenttypes.output

import java.time.OffsetDateTime
import java.util.Optional
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RosettaStoneStatementRepository {
    fun nextIdentity(): ThingId

    fun findByIdOrVersionId(id: ThingId): Optional<RosettaStoneStatement>

    fun findAll(
        pageable: Pageable,
        context: ThingId? = null,
        templateId: ThingId? = null,
        templateTargetClassId: ThingId? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null
    ): Page<RosettaStoneStatement>

    fun save(statement: RosettaStoneStatement)

    fun deleteAll()
}
