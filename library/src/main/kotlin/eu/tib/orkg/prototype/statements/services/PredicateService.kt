package eu.tib.orkg.prototype.statements.services

import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.PredicateRepresentation
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.UpdatePredicateUseCase
import eu.tib.orkg.prototype.statements.application.PredicateCantBeDeleted
import eu.tib.orkg.prototype.statements.application.PredicateNotFound
import eu.tib.orkg.prototype.statements.domain.model.Clock
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.SystemClock
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.util.EscapedRegex
import eu.tib.orkg.prototype.util.SanitizedWhitespace
import eu.tib.orkg.prototype.util.WhitespaceIgnorantPattern
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PredicateService(
    private val repository: PredicateRepository,
    private val statementRepository: StatementRepository,
    private val clock: Clock = SystemClock()
) : PredicateUseCases {
    @Transactional(readOnly = true)
    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun create(command: CreatePredicateUseCase.CreateCommand): ThingId {
        val id = if (command.id != null) ThingId(command.id) else repository.nextIdentity()
        val predicate = Predicate(
            id = id,
            label = Label.ofOrNull(command.label)?.value
                ?: throw IllegalArgumentException("Invalid label: ${command.label}"),
            createdAt = clock.now(),
            createdBy = command.contributorId ?: ContributorId.createUnknownContributor()
        )
        repository.save(predicate)
        return id
    }

    override fun create(label: String): PredicateRepresentation {
        val newThingId = create(
            CreatePredicateUseCase.CreateCommand(
                label = label
            )
        )
        return repository.findByPredicateId(newThingId).map(Predicate::toPredicateRepresentation).get()
    }

    override fun create(userId: ContributorId, label: String): PredicateRepresentation {
        val newThingId = create(
            CreatePredicateUseCase.CreateCommand(
                label = label,
                contributorId = userId
            )
        )
        return repository.findByPredicateId(newThingId).map(Predicate::toPredicateRepresentation).get()
    }

    override fun findAll(pageable: Pageable): Page<PredicateRepresentation> =
        repository.findAll(pageable).map(Predicate::toPredicateRepresentation)

    override fun findById(id: ThingId): Optional<PredicateRepresentation> =
        repository.findByPredicateId(id).map(Predicate::toPredicateRepresentation)

    override fun findAllByLabel(label: String, pageable: Pageable): Page<PredicateRepresentation> =
        repository.findAllByLabelMatchesRegex(label.toExactSearchString(), pageable)
            .map(Predicate::toPredicateRepresentation) // TODO: See declaration

    override fun findAllByLabelContaining(part: String, pageable: Pageable): Page<PredicateRepresentation> =
        repository.findAllByLabelMatchesRegex(part.toSearchString(), pageable)
            .map(Predicate::toPredicateRepresentation) // TODO: See declaration

    override fun update(id: ThingId, command: UpdatePredicateUseCase.ReplaceCommand) {
        var found = repository.findByPredicateId(id).get()

        // update all the properties
        found = found.copy(label = command.label)

        repository.save(found)
    }

    override fun createIfNotExists(id: ThingId, label: String) {
        val oPredicate = repository.findByPredicateId(id)

        if (oPredicate.isEmpty) {
            val p = Predicate(
                label = label,
                id = id,
                createdBy = ContributorId.createUnknownContributor(),
                createdAt = OffsetDateTime.now()
            )
            repository.save(p)
        }
    }

    override fun delete(predicateId: ThingId) {
        val predicate = findById(predicateId).orElseThrow { PredicateNotFound(predicateId) }

        if (statementRepository.countPredicateUsage(predicate.id) > 0)
            throw PredicateCantBeDeleted(predicate.id)

        repository.deleteByPredicateId(predicate.id)
    }

    override fun removeAll() = repository.deleteAll()

    private fun String.toSearchString() =
        "(?i).*${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}.*"

    private fun String.toExactSearchString() =
        "(?i)^${WhitespaceIgnorantPattern(EscapedRegex(SanitizedWhitespace(this)))}$"
}

fun Predicate.toPredicateRepresentation(): PredicateRepresentation = object : PredicateRepresentation {
    override val id: ThingId = this@toPredicateRepresentation.id
    override val label: String = this@toPredicateRepresentation.label
    override val description: String? = this@toPredicateRepresentation.description
    override val jsonClass: String = "predicate"
    override val createdAt: OffsetDateTime = this@toPredicateRepresentation.createdAt
    override val createdBy: ContributorId = this@toPredicateRepresentation.createdBy
}
