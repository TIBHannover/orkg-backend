package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import java.util.Optional
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, Long>, FulltextSearchUseCase<Neo4jLiteral> {

    override fun findAll(): Iterable<Neo4jLiteral>

    override fun findById(id: Long?): Optional<Neo4jLiteral>

    fun findByLiteralId(id: LiteralId?): Optional<Neo4jLiteral>

    fun findAllByLabel(value: String): Iterable<Neo4jLiteral>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String): Iterable<Neo4jLiteral>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jLiteral>
}
