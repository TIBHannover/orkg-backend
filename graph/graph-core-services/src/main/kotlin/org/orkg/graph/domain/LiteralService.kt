package org.orkg.graph.domain

import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository
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
        // Note: "xsd:foo" is a valid URI, so is everything starting with a letter followed by a colon.
        // There is no easy way around that, because other valid URIs use "prefix-like" structures, such as URNs.
        if (datatype.startsWith("xsd:").not() && datatype.toUriOrNull() == null)
            throw InvalidLiteralDatatype()
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

internal fun String.toUriOrNull(): URI? = try {
    URI(this)
} catch (_: Exception) {
    null
}
