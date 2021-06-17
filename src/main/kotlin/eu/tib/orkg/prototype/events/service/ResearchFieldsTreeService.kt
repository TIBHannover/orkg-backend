package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import java.security.Principal
import java.util.UUID

interface ResearchFieldsTreeService {
    fun addResearchFieldPath(listPath: List<String>, principal: Principal)
    fun getRFTree(userId: UUID): List<ResearchFieldsTree>
}
