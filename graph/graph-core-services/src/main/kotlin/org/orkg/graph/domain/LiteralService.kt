package org.orkg.graph.domain

import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateLiteralUseCase
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
    private val clock: Clock,
) : LiteralUseCases {
    override fun create(command: CreateLiteralUseCase.CreateCommand): ThingId {
        if (command.label.length > MAX_LABEL_LENGTH) {
            throw InvalidLiteralLabel()
        }
        // Note: "xsd:foo" is a valid URI, so is everything starting with a letter followed by a colon.
        // There is no easy way around that, because other valid URIs use "prefix-like" structures, such as URNs.
        if (command.datatype.startsWith("xsd:").not() && command.datatype.toUriOrNull() == null)
            throw InvalidLiteralDatatype()
        val id = command.id
            ?.also { id -> repository.findById(id).ifPresent { throw LiteralAlreadyExists(id) } }
            ?: repository.nextIdentity()
        val literal = Literal(
            label = command.label,
            id = id,
            datatype = command.datatype,
            createdBy = command.contributorId,
            createdAt = OffsetDateTime.now(clock),
            modifiable = command.modifiable
        )
        repository.save(literal)
        return id
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

        if (!found.modifiable)
            throw LiteralNotModifiable(found.id)

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
