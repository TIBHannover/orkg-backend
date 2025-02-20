package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.toIRIOrNull
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class LiteralService(
    private val repository: LiteralRepository,
    private val statementRepository: StatementRepository,
    private val clock: Clock,
) : LiteralUseCases {
    override fun create(command: CreateLiteralUseCase.CreateCommand): ThingId {
        if (command.label.length > MAX_LABEL_LENGTH) {
            throw InvalidLiteralLabel()
        }
        // Note: "xsd:foo" is a valid IRI, so is everything starting with a letter followed by a colon.
        // There is no easy way around that, because other valid URIs use "prefix-like" structures, such as URNs.
        val xsd = Literals.XSD.fromString(command.datatype)
        if (xsd != null && !xsd.canParse(command.label)) {
            throw InvalidLiteralLabel(command.label, command.datatype)
        } else if (command.datatype.toIRIOrNull()?.isAbsolute != true) {
            throw InvalidLiteralDatatype()
        }
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

    @TransactionalOnNeo4j(readOnly = true)
    override fun existsById(id: ThingId): Boolean = repository.existsById(id)

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
    ): Page<Literal> =
        repository.findAll(pageable, label, createdBy, createdAtStart, createdAtEnd)

    override fun findById(id: ThingId): Optional<Literal> =
        repository.findById(id)

    override fun findDOIByContributionId(id: ThingId): Optional<Literal> =
        statementRepository.findDOIByContributionId(id)

    override fun update(literal: Literal) {
        if (literal.label.length > MAX_LABEL_LENGTH) {
            throw InvalidLiteralLabel()
        }
        // already checked by service
        var found = repository.findById(literal.id).get()

        if (!found.modifiable) {
            throw LiteralNotModifiable(found.id)
        }

        // update all the properties
        found = found.copy(label = literal.label)
        found = found.copy(datatype = literal.datatype)

        repository.save(found)
    }

    override fun deleteAll() = repository.deleteAll()
}
