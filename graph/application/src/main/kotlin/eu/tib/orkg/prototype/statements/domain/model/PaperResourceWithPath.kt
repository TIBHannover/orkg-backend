package eu.tib.orkg.prototype.statements.domain.model

typealias Path = List<List<Thing>>

data class PaperResourceWithPath(
    val paper: Resource,
    var path: Path,
)
