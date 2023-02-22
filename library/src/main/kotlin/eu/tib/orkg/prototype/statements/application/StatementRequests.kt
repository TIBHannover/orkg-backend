package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

data class StatementEditRequest(
    @JsonProperty("statement_id")
    val statementId: StatementId?,

    @JsonProperty("subject_id")
    val subjectId: String?,

    @JsonProperty("predicate_id")
    val predicateId: ThingId?,

    @JsonProperty("object_id")
    val objectId: String?
)
