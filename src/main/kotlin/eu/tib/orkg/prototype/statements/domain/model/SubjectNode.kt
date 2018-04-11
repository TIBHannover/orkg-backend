package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.core.Entity
import java.time.LocalDateTime

data class SubjectNode(
    override val id: SubjectId,
    val userId: String,
    val createdAt: LocalDateTime
) :
    Entity<SubjectId>(id)
