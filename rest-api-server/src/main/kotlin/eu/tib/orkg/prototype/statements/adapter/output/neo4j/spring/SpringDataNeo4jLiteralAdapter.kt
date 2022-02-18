package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteralIdGenerator
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import java.util.*
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jLiteralAdapter(
    private val neo4jRepository: Neo4jLiteralRepository,
    private val neo4jLiteralIdGenerator: Neo4jLiteralIdGenerator
) : LiteralRepository {
    override fun nextIdentity(): LiteralId = neo4jLiteralIdGenerator.nextIdentity()

    override fun save(literal: Literal) {
        neo4jRepository.save(literal.toNeo4jLiteral())
    }

    override fun deleteAll() {
        neo4jRepository.deleteAll()
    }

    override fun findAll(): Iterable<Literal> = neo4jRepository.findAll().map(Neo4jLiteral::toLiteral)

    override fun findByLiteralId(id: LiteralId?): Optional<Literal> =
        neo4jRepository.findByLiteralId(id).map(Neo4jLiteral::toLiteral)

    override fun findAllByLabel(value: String): Iterable<Literal> =
        neo4jRepository.findAllByLabel(value).map(Neo4jLiteral::toLiteral)

    override fun findAllByLabelMatchesRegex(label: String): Iterable<Literal> =
        neo4jRepository.findAllByLabelMatchesRegex(label).map(Neo4jLiteral::toLiteral)

    override fun findAllByLabelContaining(part: String): Iterable<Literal> =
        neo4jRepository.findAllByLabelContaining(part).map(Neo4jLiteral::toLiteral)

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> =
        neo4jRepository.findDOIByContributionId(id).map(Neo4jLiteral::toLiteral)
}

private fun Literal.toNeo4jLiteral() = Neo4jLiteral().apply {
    literalId = this@toNeo4jLiteral.id
    label = this@toNeo4jLiteral.label
    datatype = this@toNeo4jLiteral.datatype
    createdBy = this@toNeo4jLiteral.createdBy
    createdBy = this@toNeo4jLiteral.createdBy
}
