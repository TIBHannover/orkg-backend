package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import org.neo4j.cypherdsl.core.ResultStatement
import org.neo4j.cypherdsl.core.StatementBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs

abstract class SpringDataNeo4jAdapter(protected open val neo4jClient: Neo4jClient) {

    protected fun <T> Neo4jClient.RecordFetchSpec<T>.paged(pageable: Pageable, countQuery: ResultStatement): Page<T> {
        val total = neo4jClient.query(countQuery.cypher)
            .fetchAs<Long>()
            .one() ?: 0
        return PageImpl(all().toList(), pageable, total)
    }

    protected fun StatementBuilder.TerminalExposesSkip.build(pageable: Pageable): ResultStatement =
        skip(pageable.offset).limit(pageable.pageSize).build() // FIXME: sorting
}
