package org.orkg.contenttypes.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource

// TODO: convert to representation and merge with SimpleAuthor?
//       could use an abstraction of AuthorList (domain object) instead of List<Author> in Paper.
data class Author(
    val name: String,
    val id: ThingId? = null,
    val identifiers: Map<String, List<String>>? = null,
    val homepage: URI? = null
)

data class ComparisonAuthor(
    val author: SimpleAuthor,
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

sealed class SimpleAuthor {
    data class ResourceAuthor(val value: Resource) : SimpleAuthor()
    data class LiteralAuthor(val value: String) : SimpleAuthor()
}

data class PaperAuthor(
    val author: SimpleAuthor,
    val papers: Int,
)
