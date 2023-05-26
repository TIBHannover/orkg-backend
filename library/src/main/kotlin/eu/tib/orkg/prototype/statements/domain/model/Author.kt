package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ComparisonAuthor(
    val author: Author,
    val info: Iterable<ComparisonAuthorInfo>,
)

data class ComparisonAuthorInfo(
    @JsonProperty("paper_id")
    val paperId: ThingId,
    @JsonProperty("author_index")
    val authorIndex: Int,
    @JsonProperty("paper_year")
    val paperYear: Int?,
)

sealed class Author {
    data class ResourceAuthor(val value: Resource) : Author()
    data class LiteralAuthor(val value: String) : Author()
}

data class PaperAuthor(
    val author: Author,
    val papers: Int,
)
