package org.orkg.contenttypes.output

import java.util.Optional
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RosettaStoneStatementRepository {
    fun nextIdentity(): ThingId

    fun findByIdOrVersionId(id: ThingId): Optional<RosettaStoneStatement>

    fun findAll(pageable: Pageable): Page<RosettaStoneStatement>

    fun save(statement: RosettaStoneStatement)

    fun deleteAll()
}
