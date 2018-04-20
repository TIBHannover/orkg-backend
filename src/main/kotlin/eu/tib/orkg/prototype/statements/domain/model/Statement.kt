package eu.tib.orkg.prototype.statements.domain.model

data class Statement(
    val subject: ResourceId,
    val predicate: PredicateId,
    val `object`: ResourceId
)
