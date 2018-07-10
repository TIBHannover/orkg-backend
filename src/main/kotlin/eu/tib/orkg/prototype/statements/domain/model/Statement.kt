package eu.tib.orkg.prototype.statements.domain.model

data class Statement(
    val statementId: Long? = null,
    val subject: ResourceId,
    val predicate: PredicateId,
    val `object`: Object
)
