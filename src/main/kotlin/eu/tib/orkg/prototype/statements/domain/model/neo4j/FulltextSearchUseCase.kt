package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.repository.NoRepositoryBean

/**
 * Supporting functions for searching in fulltext indexes.
 */
@NoRepositoryBean
interface FulltextSearchUseCase<T> {
    /**
     * Search in a full-text index.
     *
     * @param index The name of the index used for searching.
     * @param query The query string. This can be any Lucene search language expression.
     * @return The matching nodes, ordered by score.
     */
    @Query("""CALL db.index.fulltext.queryNodes({0}, {1}) YIELD node, score RETURN node""")
    fun searchLabelsInFulltextIndex(index: String, query: String): Iterable<T>
}
