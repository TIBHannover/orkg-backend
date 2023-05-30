package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.application.InvalidLiteralLabel
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.MAX_LABEL_LENGTH
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LiteralService(
    private val repository: LiteralRepository,
    private val statementRepository: StatementRepository,
) : LiteralUseCases {
    override fun create(label: String, datatype: String): Literal =
        create(ContributorId.createUnknownContributor(), label, datatype)

    override fun create(userId: ContributorId, label: String, datatype: String): Literal {
        if (label.length > MAX_LABEL_LENGTH) {
            throw InvalidLiteralLabel()
        }
        val literalId = repository.nextIdentity()
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

    @Transactional(readOnly = true)
    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun findAll(pageable: Pageable): Page<Literal> =
        repository.findAll(pageable)

    override fun findById(id: ThingId): Optional<Literal> =
        repository.findById(id)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Literal> =
        repository.findAllByLabel(labelSearchString, pageable)

    override fun findDOIByContributionId(id: ThingId): Optional<Literal> =
        statementRepository.findDOIByContributionId(id)

    override fun update(literal: Literal) {
        if (literal.label.length > MAX_LABEL_LENGTH) {
            throw InvalidLiteralLabel()
        }
        // already checked by service
        var found = repository.findById(literal.id).get()

        // update all the properties
        found = found.copy(label = literal.label)
        found = found.copy(datatype = literal.datatype)

        repository.save(found)
    }

    override fun removeAll() = repository.deleteAll()
}
