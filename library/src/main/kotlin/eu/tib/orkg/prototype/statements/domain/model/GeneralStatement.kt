package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.api.ThingRepresentation
import eu.tib.orkg.prototype.statements.application.StatementResponse
import java.time.OffsetDateTime

data class GeneralStatement(
    val id: StatementId? = null,
    val subject: Thing,
    val predicate: Predicate,
    val `object`: Thing,
    @JsonProperty("created_at")
    val createdAt: OffsetDateTime?,
    @JsonProperty("created_by")
    val createdBy: ContributorId = ContributorId.createUnknownContributor()
) : StatementResponse

interface StatementRepresentation : StatementProvenanceMetadata {
    val id: StatementId
    val subject: ThingRepresentation
    val predicate: PredicateRepresentation
    val `object`: ThingRepresentation
}

interface StatementProvenanceMetadata {
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime

    @get:JsonProperty("created_by")
    val createdBy: ContributorId
}

data class CreateStatement(
    val id: StatementId? = null,
    @JsonProperty("subject_id")
    val subjectId: ThingId,
    @JsonProperty("predicate_id")
    val predicateId: ThingId,
    @JsonProperty("object_id")
    val objectId: ThingId
)
