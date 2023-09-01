package eu.tib.orkg.prototype.statements.domain.model

typealias Path = kotlin.collections.List<kotlin.collections.List<Thing>>

data class PaperResourceWithPath(
    val paper: Resource,
    var path: Path,
)
