package org.orkg.contenttypes.input

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveRosettaStoneTemplateUseCase {
    fun findById(id: ThingId): Optional<RosettaStoneTemplate>
    fun findAll(
        searchString: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        pageable: Pageable
    ): Page<RosettaStoneTemplate>
}
