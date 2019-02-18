package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.*

data class Statement(
    val statementId: StatementId? = null,
    @JsonProperty("subject_id")
    val subjectId: ResourceId,
    @JsonProperty("predicate_id")
    val predicateId: PredicateId,
    val `object`: Object
) : Comparable<Statement> {
    override fun compareTo(other: Statement): Int {
        return when {
            subjectId < other.subjectId -> -1
            subjectId > other.subjectId -> 1
            else -> {
                when {
                    predicateId < other.predicateId -> -1
                    predicateId > other.predicateId -> 1
                    else -> `object`.compareTo(other.`object`)
                }
            }
        }
    }
}
