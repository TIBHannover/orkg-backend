package eu.tib.prinzm.orkg.prototype.publication.domain.model

import java.net.URI


data class Article(val uri: URI)

interface ArticleRepository {
    fun findAll(): Collection<Article>
}
