package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteralIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteralRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jLiteralService(
    private val neo4jLiteralRepository: Neo4jLiteralRepository,
    private val neo4jLiteralIdGenerator: Neo4jLiteralIdGenerator
) : LiteralService {
    override fun create(label: String, datatype: String) = create(UUID(0, 0), label, datatype)

    override fun create(userId: UUID, label: String, datatype: String): Literal {
        val literalId = neo4jLiteralIdGenerator.nextIdentity()
        return neo4jLiteralRepository
            .save(Neo4jLiteral(label = label, literalId = literalId, datatype = datatype, createdBy = userId))
            .toLiteral()
    }

    override fun findAll() = neo4jLiteralRepository.findAll()
        .map(Neo4jLiteral::toLiteral)

    override fun findById(id: LiteralId?): Optional<Literal> =
        neo4jLiteralRepository.findByLiteralId(id)
            .map(Neo4jLiteral::toLiteral)

    override fun findAllByLabel(label: String) =
        neo4jLiteralRepository.findAllByLabelMatchesRegex("(?i)^${escapeRegexString(label)}$") // TODO: See declaration
            .map(Neo4jLiteral::toLiteral)

    override fun findAllByLabelContaining(part: String) =
        neo4jLiteralRepository.findAllByLabelMatchesRegex("(?i).*${escapeRegexString(part)}.*") // TODO: See declaration
            .map(Neo4jLiteral::toLiteral)

    override fun update(literal: Literal): Literal {
        // already checked by service
        val found = neo4jLiteralRepository.findByLiteralId(literal.id).get()

        // update all the properties
        found.label = literal.label
        found.datatype = literal.datatype

        return neo4jLiteralRepository.save(found).toLiteral()
    }
}
