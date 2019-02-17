package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*
import java.util.*

@Service
@Transactional
class Neo4jLiteralService(
    private val neo4jLiteralRepository: Neo4jLiteralRepository
) : LiteralService {
    override fun create(label: String): Literal {
        val literalId = neo4jLiteralRepository.nextIdentity()
        return neo4jLiteralRepository
            .save(Neo4jLiteral(label = label, literalId = literalId))
            .toLiteral()
    }

    override fun findAll() = neo4jLiteralRepository.findAll()
        .map(Neo4jLiteral::toLiteral)

    override fun findById(id: LiteralId?): Optional<Literal> =
        neo4jLiteralRepository.findByLiteralId(id)
            .map(Neo4jLiteral::toLiteral)

    override fun findAllByLabel(label: String) =
        neo4jLiteralRepository.findAllByLabel(label)
            .map(Neo4jLiteral::toLiteral)

    override fun findAllByLabelContaining(part: String) =
        neo4jLiteralRepository.findAllByLabelContaining(part)
            .map(Neo4jLiteral::toLiteral)

    override fun update(literal: Literal): Literal {
        // already checked by service
        val found = neo4jLiteralRepository.findByLiteralId(literal.id).get()

        // update all the properties
        found.label = literal.label

        return neo4jLiteralRepository.save(found).toLiteral()
    }
}
