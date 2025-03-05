package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.toIRIOrNull
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UpdateLiteralUseCase
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class LiteralService(
    private val repository: LiteralRepository,
    private val statementRepository: StatementRepository,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : LiteralUseCases {
    override fun create(command: CreateLiteralUseCase.CreateCommand): ThingId {
        if (command.label.length > MAX_LABEL_LENGTH) {
            throw InvalidLiteralLabel()
        }
        validateLabel(command.label, command.datatype)
        command.id?.also { id -> repository.findById(id).ifPresent { throw LiteralAlreadyExists(id) } }
        return unsafeLiteralUseCases.create(command)
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

    override fun update(command: UpdateLiteralUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        command.label?.also { label ->
            if (label.length > MAX_LABEL_LENGTH) {
                throw InvalidLiteralLabel()
            }
        }
        val literal = repository.findById(command.id)
            .orElseThrow { LiteralNotFound(command.id) }
        if (!literal.modifiable) {
            throw LiteralNotModifiable(literal.id)
        }
        val updated = literal.apply(command)
        if (literal.label != updated.label || literal.datatype != updated.datatype) {
            validateLabel(updated.label, updated.datatype)
        }
        if (updated != literal) {
            repository.save(updated)
        }
    }

    override fun deleteAll() = repository.deleteAll()

    // Note: "xsd:foo" is a valid IRI, so is everything starting with a letter followed by a colon.
    // There is no easy way around that, because other valid URIs use "prefix-like" structures, such as URNs.
    private fun validateLabel(value: String, datatype: String) {
        val xsd = Literals.XSD.fromString(datatype)
        if (xsd != null && !xsd.canParse(value)) {
            throw InvalidLiteralLabel(value, datatype)
        } else if (datatype.toIRIOrNull()?.isAbsolute != true) {
            throw InvalidLiteralDatatype()
        }
    }
}
