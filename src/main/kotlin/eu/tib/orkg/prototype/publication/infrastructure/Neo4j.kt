package eu.tib.orkg.prototype.publication.infrastructure

import eu.tib.orkg.prototype.publication.domain.model.Article
import eu.tib.orkg.prototype.publication.domain.model.ArticleRepository
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository
import java.net.URI


/**
 * Spring Data Neo4j (SDN) representation of an article.
 *
 * Not all properties/elements of the ontology are currently supported.
 */
@NodeEntity(label = "swrc__Article")
class Neo4jArticle {
    @Id
    @GeneratedValue
    var id: Long? = null

    var uri: String? = null
}

interface Neo4jArticleRepository :
    Neo4jRepository<Neo4jArticle, Long> {
    @Query("MATCH (n:`swrc__Article`) RETURN n")
    override fun findAll(): MutableIterable<Neo4jArticle>?
}

@Repository
class Neo4jArticleRepositoryWrapper : ArticleRepository {

    @Autowired
    private lateinit var repository: Neo4jArticleRepository

    override fun findAll(): Collection<Article> {
        val results = repository.findAll()
        results?.let {
            return results.map { Article(URI(it.uri)) }
        }
        return setOf()
    }
}
