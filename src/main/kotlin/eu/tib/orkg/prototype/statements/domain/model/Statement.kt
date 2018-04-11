package eu.tib.orkg.prototype.statements.domain.model

data class Statement(
    val subj: SubjectNode,
    val rel: Predicate,
    val obj: ObjectNode
)
