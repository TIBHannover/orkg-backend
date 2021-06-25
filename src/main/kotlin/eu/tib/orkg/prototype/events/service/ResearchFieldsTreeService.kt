package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import java.security.Principal
import java.util.UUID

interface ResearchFieldsTreeService {
    fun addResearchFieldPath(resourceId: String, userId: UUID)
    fun getRFTree(userId: UUID): List<ResearchFieldsTree>
    fun getResearchFieldByUser(resourceId: String, userId: UUID): ResearchFieldsTree
    fun isResearchFieldPresent(resourceId: String, userId: UUID): Boolean
    fun unfollowResearchFields(resourceId: String, userId: UUID): List<ResearchFieldsTree>

}
