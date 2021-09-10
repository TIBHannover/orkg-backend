package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteralRepository
import eu.tib.orkg.prototype.statements.ports.LiteralRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class LiteralPersistenceAdapter(
    private val neo4jLiteralRepository: Neo4jLiteralRepository
): LiteralRepository {

    override fun findAll() = neo4jLiteralRepository.findAll()
        .map(Neo4jLiteral::toLiteral)

    override fun findById(id: LiteralId?): Optional<Literal> =
        neo4jLiteralRepository.findByLiteralId(id)
            .map(Neo4jLiteral::toLiteral)

    fun findAllById(ids: List<LiteralId>): Iterable<Literal> =
        neo4jLiteralRepository.findAllByLiteralIdIn(ids)
            .map(Neo4jLiteral::toLiteral)

    override fun findAllByLabel(label: String) =
        neo4jLiteralRepository.findAllByLabelMatchesRegex(label.toExactSearchString())
            .map(Neo4jLiteral::toLiteral)

    override fun findAllByLabelContaining(part: String) =
        neo4jLiteralRepository.findAllByLabelMatchesRegex(part.toSearchString())
            .map(Neo4jLiteral::toLiteral)

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> =
        neo4jLiteralRepository.findDOIByContributionId(id.toString())
            .map(Neo4jLiteral::toLiteral)

    private fun String.toSearchString() = "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() = "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"

}
