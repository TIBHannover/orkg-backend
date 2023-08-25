package eu.tib.orkg.prototype.statements.services

import dev.forkhandles.values.ofOrNull
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.UpdatePredicateUseCase
import eu.tib.orkg.prototype.statements.application.PredicateCantBeDeleted
import eu.tib.orkg.prototype.statements.application.PredicateNotFound
import eu.tib.orkg.prototype.statements.domain.model.Label
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.Clock
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
    private val clock: Clock = Clock.systemDefaultZone(),
) : PredicateUseCases {
    @Transactional(readOnly = true)
    override fun exists(id: ThingId): Boolean = repository.exists(id)

    override fun create(command: CreatePredicateUseCase.CreateCommand): ThingId {
        val id = if (command.id != null) ThingId(command.id) else repository.nextIdentity()
        val predicate = Predicate(
            id = id,
            label = Label.ofOrNull(command.label)?.value
                ?: throw IllegalArgumentException("Invalid label: ${command.label}"),
            createdAt = OffsetDateTime.now(clock),
            createdBy = command.contributorId ?: ContributorId.createUnknownContributor()
        )
        repository.save(predicate)
        return id
    }

    override fun create(label: String): Predicate {
        val newThingId = create(
            CreatePredicateUseCase.CreateCommand(
                label = label
            )
        )
        return repository.findById(newThingId).get()
    }

    override fun create(userId: ContributorId, label: String): Predicate {
        val newThingId = create(
            CreatePredicateUseCase.CreateCommand(
                label = label,
                contributorId = userId
            )
        )
        return repository.findById(newThingId).get()
    }

    override fun findAll(pageable: Pageable): Page<Predicate> =
        repository.findAll(pageable)

    override fun findById(id: ThingId): Optional<Predicate> =
        repository.findById(id)

    override fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Predicate> =
        repository.findAllByLabel(labelSearchString, pageable)

    override fun update(id: ThingId, command: UpdatePredicateUseCase.ReplaceCommand) {
        var found = repository.findById(id).get()

        // update all the properties
        found = found.copy(label = command.label)

        repository.save(found)
    }

    override fun createIfNotExists(id: ThingId, label: String) {
        val oPredicate = repository.findById(id)

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

        repository.deleteById(predicate.id)
    }

    override fun removeAll() = repository.deleteAll()
}
