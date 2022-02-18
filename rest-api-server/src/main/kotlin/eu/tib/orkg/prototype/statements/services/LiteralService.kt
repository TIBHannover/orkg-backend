package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
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
    override fun create(label: String, datatype: String) =
        create(ContributorId.createUnknownContributor(), label, datatype)

    override fun create(userId: ContributorId, label: String, datatype: String): Literal {
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
        return newLiteral
    }

    override fun findAll() = repository.findAll()

    override fun findById(id: LiteralId?): Optional<Literal> = repository.findByLiteralId(id)

    override fun findAllByLabel(label: String) =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString()) // TODO: See declaration

    override fun findAllByLabelContaining(part: String) =
        repository.findAllByLabelMatchesRegex(part.toSearchString()) // TODO: See declaration

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> = repository.findDOIByContributionId(id)

    override fun update(literal: Literal): Literal {
        // already checked by service
        var found = repository.findByLiteralId(literal.id).get()

        // update all the properties
        found = found.copy(label = literal.label)
        found = found.copy(datatype = literal.datatype)

        repository.save(found)
        return found
    }

    override fun removeAll() = repository.deleteAll()

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}
