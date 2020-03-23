package eu.tib.orkg.prototype.statements.domain.model

data class Stats(
    val statements: Long,
    val resources: Long,
    val predicates: Long,
    val literals: Long,
    val papers: Long,
    val classes: Long,
    val contributions: Long,
    val fields: Long,
    val problems: Long
)
