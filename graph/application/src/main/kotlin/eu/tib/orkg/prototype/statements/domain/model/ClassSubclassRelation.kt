package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime

data class ClassSubclassRelation(
    val child: Class,
    val parent: Class,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor()
)
