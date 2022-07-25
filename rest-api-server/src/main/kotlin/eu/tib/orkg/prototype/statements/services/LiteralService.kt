package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.LiteralRepresentation
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.time.OffsetDateTime
import java.util.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LiteralService(
    private val repository: LiteralRepository,
) : LiteralUseCases {
    override fun create(label: String, datatype: String): LiteralRepresentation =
        create(ContributorId.createUnknownContributor(), label, datatype)

    override fun create(userId: ContributorId, label: String, datatype: String): LiteralRepresentation {
        var literalId = repository.nextIdentity()

        // Should be moved to the Generator in the future
        while (repository.findByLiteralId(literalId).isPresent) {
            literalId = repository.nextIdentity()
        }
        val newLiteral = Literal(
            label = label,
            id = literalId,
            datatype = datatype,
            createdBy = userId,
            createdAt = OffsetDateTime.now(),
        )
        repository.save(newLiteral)
        return newLiteral.toLiteralRepresentation()
    }

    override fun findAll(): Iterable<LiteralRepresentation> = repository.findAll().map(Literal::toLiteralRepresentation)

    override fun findById(id: LiteralId?): Optional<LiteralRepresentation> =
        repository.findByLiteralId(id).map(Literal::toLiteralRepresentation)

    override fun findAllByLabel(label: String): Iterable<LiteralRepresentation> =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString())
            .map(Literal::toLiteralRepresentation) // TODO: See declaration

    override fun findAllByLabelContaining(part: String): Iterable<LiteralRepresentation> =
        repository.findAllByLabelMatchesRegex(part.toSearchString())
            .map(Literal::toLiteralRepresentation) // TODO: See declaration

    override fun findDOIByContributionId(id: ResourceId): Optional<LiteralRepresentation> =
        repository.findDOIByContributionId(id).map(Literal::toLiteralRepresentation)

    override fun update(literal: Literal): LiteralRepresentation {
        // already checked by service
        var found = repository.findByLiteralId(literal.id).get()

        // update all the properties
        found = found.copy(label = literal.label)
        found = found.copy(datatype = literal.datatype)

        repository.save(found)
        return found.toLiteralRepresentation()
    }

    override fun removeAll() = repository.deleteAll()

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}

fun Literal.toLiteralRepresentation(): LiteralRepresentation = object : LiteralRepresentation {
    override val id: LiteralId = this@toLiteralRepresentation.id!!
    override val label: String = this@toLiteralRepresentation.label
    override val datatype: String = this@toLiteralRepresentation.datatype
    override val jsonClass: String = "literal"
    override val createdAt: OffsetDateTime = this@toLiteralRepresentation.createdAt
    override val createdBy: ContributorId = this@toLiteralRepresentation.createdBy
}
