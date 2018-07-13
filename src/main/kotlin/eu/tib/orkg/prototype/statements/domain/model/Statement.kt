package eu.tib.orkg.prototype.statements.domain.model

import java.time.LocalDateTime

data class Statement(
    val statementId: Long? = null,
    val subject: ResourceId,
    val predicate: PredicateId,
    val `object`: Object
) : Comparable<Statement> {
    val created: LocalDateTime = LocalDateTime.now()

    override fun compareTo(other: Statement): Int {
        return when {
            subject < other.subject -> -1
            subject > other.subject -> 1
            else -> {
                when {
                    predicate < other.predicate -> -1
                    predicate > other.predicate -> 1
                    else -> `object`.compareTo(other.`object`)
                }
            }
        }
    }
}
