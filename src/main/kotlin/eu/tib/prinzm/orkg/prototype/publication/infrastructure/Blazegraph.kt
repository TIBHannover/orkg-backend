package eu.tib.prinzm.orkg.prototype.publication.infrastructure

import eu.tib.prinzm.orkg.prototype.publication.domain.model.Article
import eu.tib.prinzm.orkg.prototype.publication.domain.model.ArticleRepository
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.QueryResults
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.net.URI


@Repository
@Primary
/**
 * **THIS IS NOT PRODUCTION READY! DO NOT USE!**
 */
class BlazegraphSparqlArticleRepository :
    ArticleRepository {

    private val repo: SPARQLRepository =
        SPARQLRepository("http://localhost:8889/bigdata/sparql").apply {
            initialize()
        }

    override fun findAll(): Collection<Article> {
        repo.connection.use { connection ->
            val tupleQuery = connection.prepareTupleQuery(
                QueryLanguage.SPARQL,
                Queries.ALL_ARTICLES
            )

            val result = tupleQuery.evaluate()

            val names = result.bindingNames

            val results = try {
                QueryResults.asList(result)
            } catch (e: Exception) {
                listOf<BindingSet>()
            }

            return results.map {
                it.getValue(names[0]).stringValue()
                    .toArticle()
            }
        }
    }

    fun String.toArticle() = Article(URI(this))

    object Queries {
        const val ALL_ARTICLES = """
            prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            prefix swrc: <http://swrc.ontoware.org/ontology#>

            select ?article
            where {
                ?article rdf:type swrc:Article ;

            }
            """
    }
}
