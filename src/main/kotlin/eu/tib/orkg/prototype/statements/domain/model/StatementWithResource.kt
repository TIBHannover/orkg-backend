package eu.tib.orkg.prototype.statements.domain.model

data class StatementWithResource(
    val id: Long,
    val subject: Resource,
    val predicate: Predicate,
    val `object`: ResourceObject
)
