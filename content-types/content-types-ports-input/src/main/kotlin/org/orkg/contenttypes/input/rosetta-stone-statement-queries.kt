package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveRosettaStoneStatementUseCase {
    fun findByIdOrVersionId(id: ThingId): Optional<RosettaStoneStatement>
    fun findAll(pageable: Pageable): Page<RosettaStoneStatement>
}
